/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001 - 2016 Object Refinery Ltd, Pentaho Corporation and Contributors..  All rights reserved.
 */
package org.pentaho.reporting.engine.classic.core.util;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.ReportElement;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.libraries.xmlns.writer.XmlWriter;

public class RotationUtils {
  private static final Log logger = LogFactory.getLog( RotationUtils.class );
  public static final float NO_ROTATION = 0; // degrees
  public static final float FULL_ROTATION = 360; // degrees

  public static final String ROTATE_LEFT = "left"; //$NON-NLS-1$
  public static final String ROTATE_RIGHT = "right"; //$NON-NLS-1$

  public static final String ROTATE_NONE = "none"; //$NON-NLS-1$
  public static final String ROTATE_NULL = "null"; //$NON-NLS-1$

  public static boolean hasRotation( RenderBox content ) {
    return NO_ROTATION != getRotation( content );
  }

  public static boolean hasRotation( float rotation ) {
    return NO_ROTATION != rotation;
  }

  public static float getRotation( RenderBox content ) {
    if ( content == null || content.getAttributes() == null ) {
      return NO_ROTATION;
    }

    return getRotation(
      (String) content.getAttributes().getAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.ROTATION ) );
  }

  public static String getRotationAsString( RenderBox content ) {
    return String.valueOf( getRotation( content ) );
  }

  public static boolean isRotationOverXaxis( float rotation ) {
    return rotation == 90 || rotation == -90 || rotation == 270 || rotation == -270;
  }

  public static XmlWriter closeRotationDiv( XmlWriter xmlWriter ) throws IOException {

    if ( xmlWriter == null ) {
      return xmlWriter;
    }

    xmlWriter.writeText( "</div>\n" );

    return xmlWriter;
  }

  public static float getRotation( final ReportElement e ) {

    return ( e == null ) ? NO_ROTATION :
      getRotation( String.valueOf( e.getAttribute( AttributeNames.Core.NAMESPACE, AttributeNames.Core.ROTATION ) ) );
  }

  public static String getRotationAsString( final ReportElement e ) {
    return String.valueOf( getRotation( e ) );
  }

  public static float getRotation( final String r ) {

    if ( r != null && !ROTATE_NONE.equalsIgnoreCase( r ) && !ROTATE_NULL.equalsIgnoreCase( r ) ) {

      if ( ROTATE_LEFT.equalsIgnoreCase( r ) ) {
        return Float.valueOf( 90 );

      } else if ( ROTATE_RIGHT.equalsIgnoreCase( r ) ) {
        return Float.valueOf( -90 );

      } else if ( isValidNumber( r ) ) {
        // Check if rotation is needed by validating the rotation angle value
        return Float.valueOf( r ).floatValue() % FULL_ROTATION; // remainder
      }
    }
    return NO_ROTATION;
  }

  /*
   * xls throws exception if angle out of range [-90,90]
   */
  public static float getRotationDegreesInXlsAcceptedRange( final String r ) {

    final float rotation = getRotation( r );
    float mappedRotation = NO_ROTATION;
    // Accepted range is [-90,90] degrees
    if ( rotation >= -90 && rotation <= 90 ) {
      mappedRotation = rotation;
    } else if ( rotation >= 270 && rotation < 360 ) {
      mappedRotation = rotation - 360;
    } else if ( rotation > -360 && rotation <= -270 ) {
      mappedRotation = rotation + 360;
    }

    logger.info( "Requested rotation of " + rotation + " degrees will be mapped to " + mappedRotation
      + ", to comply to the XLS accepted range of [-90,90] degrees" );

    return mappedRotation;

  }

  /*
   * poi 3.12 FINAL has one issue with negative rotation of Xlsx;
   *
   * Suggested workaround is to convert the negative value into
   * a corresponding range between [0 , 180] degrees
   *
   * @see https://bz.apache.org/bugzilla/show_bug.cgi?id=58043
    *
   */
  public static float getRotationDegreesInXlsxAcceptedRange( final String r ) {
    final float rotation = getRotation( r );
    float mappedRotation = NO_ROTATION;

    if ( rotation >= 0 && rotation <= 90 ) {
      mappedRotation = rotation;
    } else if ( rotation >= -90 && rotation < 0 ) {
      mappedRotation = Math.abs( rotation - 90 );
    } else if ( rotation >= 270 && rotation < 360 ) {
      mappedRotation = 450 - rotation;
    }

    logger.info( "Requested rotation of " + rotation + " degrees will be mapped to " + mappedRotation
      + ", to comply to the XLSX accepted range of [0,180] degrees" );

    return mappedRotation;
  }

  public static boolean isValidNumber( String value ) {
    return value != null && value.matches( "[-+]?[0-9]*\\.?[0-9]+" ); // Is a number - int, float, double
  }

  public static float[] getRotationMatrix( float rotation ) {

    float[] matrix = new float[] { NO_ROTATION, NO_ROTATION, NO_ROTATION, NO_ROTATION };

    if ( rotation == NO_ROTATION ) {
      return matrix;
    }

    matrix[ 0 ] = new Double( ( Math.round( 1e5 * Math.cos( rotation * Math.PI / 180d ) ) / 1e5 ) ).floatValue();
    matrix[ 1 ] = new Double( ( Math.round( 1e5 * Math.sin( rotation * Math.PI / 180d ) ) / 1e5 ) ).floatValue();
    matrix[ 2 ] = new Double( ( Math.round( -1e5 * Math.sin( rotation * Math.PI / 180d ) ) / 1e5 ) ).floatValue();
    matrix[ 3 ] = new Double( ( Math.round( 1e5 * Math.cos( rotation * Math.PI / 180d ) ) / 1e5 ) ).floatValue();

    return matrix;
  }

  public static boolean isVerticalOrientation( RenderBox box ) {
    return box != null && isVerticalOrientation( getRotation( box ) );
  }

  public static boolean isVerticalOrientation( float rotation ) {
    return Math.abs( rotation ) == 90 || Math.abs( rotation ) == 270;
  }

  public static boolean isHorizontalOrientation( RenderBox box ) {
    return box != null && isHorizontalOrientation( getRotation( box ) );
  }

  public static boolean isHorizontalOrientation( float rotation ) {
    return Math.abs( rotation ) == NO_ROTATION || Math.abs( rotation ) == 180;
  }

  public static long calculateBoxHeight( RenderBox box ) {

    long height = 0;

    if ( box != null ) {

      boolean hasParent = box.getParent() != null;
      RenderBox parent = hasParent ? box.getParent() : null;

      height = box.getHeight();

      // remove padding from the box height
      if ( box.getBoxDefinition() != null && box.getBoxDefinition().getPaddingTop() > 0 ) {
        height -= box.getBoxDefinition().getPaddingTop();
      } else if ( hasParent && parent.getBoxDefinition() != null ) {
        height -= parent.getBoxDefinition().getPaddingTop();
      }

      if ( box.getBoxDefinition() != null && box.getBoxDefinition().getPaddingBottom() > 0 ) {
        height -= box.getBoxDefinition().getPaddingBottom();
      } else if ( hasParent && parent.getBoxDefinition() != null ) {
        height -= parent.getBoxDefinition().getPaddingBottom();
      }

      // remove border width from the box height
      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getBorderTop() > 0 ) {
        height -= box.getStaticBoxLayoutProperties().getBorderTop();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        height -= parent.getStaticBoxLayoutProperties().getBorderTop();
      }

      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getBorderBottom() > 0 ) {
        height -= box.getStaticBoxLayoutProperties().getBorderBottom();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        height -= parent.getStaticBoxLayoutProperties().getBorderBottom();
      }

      // remove margin width from the box height
      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getMarginTop() > 0 ) {
        height -= box.getStaticBoxLayoutProperties().getMarginTop();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        height -= parent.getStaticBoxLayoutProperties().getMarginTop();
      }

      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getMarginBottom() > 0 ) {
        height -= box.getStaticBoxLayoutProperties().getMarginBottom();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        height -= parent.getStaticBoxLayoutProperties().getMarginBottom();
      }
    }

    return height;
  }

  public static long calculateBoxWidth( RenderBox box ) {

    long width = 0;

    if ( box != null ) {

      boolean hasParent = box.getParent() != null;
      RenderBox parent = hasParent ? box.getParent() : null;

      width = box.getWidth();

      // remove padding from the box height
      if ( box.getBoxDefinition() != null && box.getBoxDefinition().getPaddingLeft() > 0 ) {
        width -= box.getBoxDefinition().getPaddingLeft();
      } else if ( hasParent && parent.getBoxDefinition() != null ) {
        width -= parent.getBoxDefinition().getPaddingLeft();
      }

      if ( box.getBoxDefinition() != null && box.getBoxDefinition().getPaddingRight() > 0 ) {
        width -= box.getBoxDefinition().getPaddingRight();
      } else if ( hasParent && parent.getBoxDefinition() != null ) {
        width -= parent.getBoxDefinition().getPaddingRight();
      }

      // remove border width from the box height
      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getBorderLeft() > 0 ) {
        width -= box.getStaticBoxLayoutProperties().getBorderLeft();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        width -= parent.getStaticBoxLayoutProperties().getBorderLeft();
      }

      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getBorderRight() > 0 ) {
        width -= box.getStaticBoxLayoutProperties().getBorderRight();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        width -= parent.getStaticBoxLayoutProperties().getBorderRight();
      }

      // remove margin width from the box height
      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getMarginLeft() > 0 ) {
        width -= box.getStaticBoxLayoutProperties().getMarginLeft();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        width -= parent.getStaticBoxLayoutProperties().getMarginLeft();
      }

      if ( box.getStaticBoxLayoutProperties() != null && box.getStaticBoxLayoutProperties().getMarginRight() > 0 ) {
        width -= box.getStaticBoxLayoutProperties().getMarginRight();
      } else if ( hasParent && parent.getStaticBoxLayoutProperties() != null ) {
        width -= parent.getStaticBoxLayoutProperties().getMarginRight();
      }
    }
    return width;
  }

  public static boolean isXlsType( Workbook workbook ) {
    return ( workbook != null && workbook instanceof HSSFWorkbook );
  }

  public static boolean isXlsxType( Workbook workbook ) {
    return ( workbook != null && workbook instanceof XSSFWorkbook );
  }

}
