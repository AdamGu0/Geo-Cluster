/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.WaypointRenderer;

/**
 *
 * @author AdamGu0
 */
public class MapKit extends JXMapKit {

    private final WaypointPainter waypointPainter;
    private Set<MyWaypoint> waypoints;
    public MarkerClusterer markerClusterer;

    public MapKit() {
        super();
        TileFactoryInfo info = new OSMTileFactoryInfo();

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(8);
        this.setTileFactory(tileFactory);

        this.setMiniMapVisible(false);
        GeoPosition shanghai = new GeoPosition(31.13, 121.48);
        this.setZoom(9);
        this.setCenterPosition(shanghai);

        waypointPainter = new WaypointPainter();
        waypointPainter.setRenderer(new MyWaypointRenderer());
        this.getMainMap().setOverlayPainter(waypointPainter);
        markerClusterer = new MarkerClusterer(this);
    }

    public void setWaypoints(Cluster[] clusters) {
        if (clusters == null) {
            waypoints = new HashSet<MyWaypoint>(0);
        } else {
            waypoints = new HashSet<MyWaypoint>(clusters.length * 2);
            for (Cluster c : clusters) {
                GeoPosition p = new GeoPosition(c.centroid.vectors[1], c.centroid.vectors[0]);
                MyWaypoint w = new MyWaypoint(c.pointsList.size(), p);
                waypoints.add(w);
            }
        }
        waypointPainter.setWaypoints(waypoints);
    }
    
    public void adjustMapByWaypoints() {
        Set<GeoPosition> positions = new HashSet<>(waypoints.size() * 2);
        for (MyWaypoint w : waypoints) {
            positions.add(w.getPosition());
        }
        getMainMap().zoomToBestFit(positions, 0.9);
    }
}

class MyWaypoint extends DefaultWaypoint {

    protected int amount;
    protected final Color color;

    public MyWaypoint(int amount, GeoPosition coord) {
        super(coord);
        this.amount = amount;
        if (amount <= 20) {
            this.color = new Color(0, 255, 0, 42);
        } else if (amount <= 50) {
            this.color = new Color(127, 255, 0, 84);
        } else if (amount <= 200) {
            this.color = new Color(255, 255, 0, 126);
        } else if (amount <= 1000) {
            this.color = new Color(255, 127, 0, 168);
        } else if (amount <= 5000) {
            this.color = new Color(255, 0, 0, 210);
        } else {
            this.color = new Color(255, 0, 0, 252);
        }
    }
}

class MyWaypointRenderer implements WaypointRenderer<MyWaypoint> {

    private static final Log log = LogFactory.getLog(MyWaypointRenderer.class);

    private final Map<Color, BufferedImage> map = new HashMap<Color, BufferedImage>();

//	private final Font font = new Font("Lucida Sans", Font.BOLD, 10);
    private BufferedImage origImage;

    /**
     * Uses a default waypoint image
     */
    public MyWaypointRenderer() {
        URL resource = getClass().getResource("waypoint_white.png");

        try {
            origImage = ImageIO.read(resource);
        } catch (Exception ex) {
            log.warn("couldn't read waypoint_white.png", ex);
        }
    }

    private BufferedImage convert(BufferedImage loadImg, Color newColor) {
        int w = loadImg.getWidth();
        int h = loadImg.getHeight();
        BufferedImage imgOut = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage imgColor = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = imgColor.createGraphics();
        g.setColor(newColor);
        g.fillRect(0, 0, w + 1, h + 1);
        g.dispose();

        Graphics2D graphics = imgOut.createGraphics();
        graphics.drawImage(loadImg, 0, 0, null);
        graphics.setComposite(MultiplyComposite.Default);
        graphics.drawImage(imgColor, 0, 0, null);
        graphics.dispose();

        return imgOut;
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer viewer, MyWaypoint w) {
                    g = (Graphics2D) g.create();

            if (origImage == null) {
                return;
            }

            BufferedImage myImg = map.get(w.color);

            if (myImg == null) {
                myImg = convert(origImage, w.color);
                map.put(w.color, myImg);
            }

            Point2D point = viewer.getTileFactory().geoToPixel(w.getPosition(), viewer.getZoom());

            int x = (int) point.getX();
            int y = (int) point.getY();

            g.drawImage(myImg, x - myImg.getWidth() / 2, y - myImg.getHeight(), null);

            String label = String.valueOf(w.amount);

//		g.setFont(font);
            FontMetrics metrics = g.getFontMetrics();
            int tw = metrics.stringWidth(label);
            int th = 1 + metrics.getAscent();

//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawString(label, x - tw / 2, y + th);

            g.dispose();
    }
}

class MultiplyComposite implements Composite {

    /**
     * The default implementation
     */
    public static final MultiplyComposite Default = new MultiplyComposite();

    private MultiplyComposite() {
        // empty
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new CompositeContext() {
            @Override
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
                if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT
                        || dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT
                        || dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
                    throw new IllegalStateException("Source and destination must store pixels as INT.");
                }

                int width = Math.min(src.getWidth(), dstIn.getWidth());
                int height = Math.min(src.getHeight(), dstIn.getHeight());

                int[] srcPixel = new int[4];
                int[] dstPixel = new int[4];
                int[] srcPixels = new int[width];
                int[] dstPixels = new int[width];

                for (int y = 0; y < height; y++) {
                    src.getDataElements(0, y, width, 1, srcPixels);
                    dstIn.getDataElements(0, y, width, 1, dstPixels);

                    for (int x = 0; x < width; x++) {
                        // pixels are stored as INT_ARGB
                        // our arrays are [R, G, B, A]
                        int pixel = srcPixels[x];
                        srcPixel[0] = (pixel >> 16) & 0xFF;
                        srcPixel[1] = (pixel >> 8) & 0xFF;
                        srcPixel[2] = (pixel >> 0) & 0xFF;
                        srcPixel[3] = (pixel >> 24) & 0xFF;

                        pixel = dstPixels[x];
                        dstPixel[0] = (pixel >> 16) & 0xFF;
                        dstPixel[1] = (pixel >> 8) & 0xFF;
                        dstPixel[2] = (pixel >> 0) & 0xFF;
                        dstPixel[3] = (pixel >> 24) & 0xFF;

                        int[] result = new int[]{
                            (srcPixel[0] * dstPixel[0]) >> 8,
                            (srcPixel[1] * dstPixel[1]) >> 8,
                            (srcPixel[2] * dstPixel[2]) >> 8,
                            (srcPixel[3] * dstPixel[3]) >> 8
                        };

                        // mixes the result with the opacity
                        dstPixels[x]
                                = (result[3]) << 24
                                | (result[0]) << 16
                                | (result[1]) << 8
                                | (result[2]);
                    }
                    dstOut.setDataElements(0, y, width, 1, dstPixels);
                }
            }

            @Override
            public void dispose() {
                // empty
            }
        };
    }
}
