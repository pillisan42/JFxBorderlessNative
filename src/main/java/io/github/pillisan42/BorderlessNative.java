package io.github.pillisan42;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * The type Borderless native.
 */
public class BorderlessNative {

    public static final String THIRTY_TWO = "32";
    public static final String SIXTY_FOUR = "64";

    public static final String JAVA8_32_BITS_DLL = "java8/x86/JFxBorderlessNative.dll";

    public static final String JAVA8_32_BITS_DLL_RESOURCE = "/libs/" + JAVA8_32_BITS_DLL;

    public static final String JAVA8_64_BITS_DLL = "java8/x64/JFxBorderlessNative.dll";

    public static final String JAVA8_64_BITS_DLL_RESOURCE = "/libs/" + JAVA8_64_BITS_DLL;

    /**
     * Load jar dll.
     *
     * @param targetFolderNullable the target folder to copy dll
     */
    public static void loadJarDll(String targetFolderNullable) {
        File targetFolderNonNull;
        String arch = System.getProperty("sun.arch.data.model");
        String sourceLocation = null;
        String dllLocation = null;
        if (Objects.equals(arch, THIRTY_TWO)) {
            sourceLocation = JAVA8_32_BITS_DLL_RESOURCE;
            dllLocation = JAVA8_32_BITS_DLL;
        } else if (Objects.equals(arch, SIXTY_FOUR)) {
            sourceLocation = JAVA8_64_BITS_DLL_RESOURCE;
            dllLocation = JAVA8_64_BITS_DLL;
        } else {
            System.err.println("Failed to determine architecture to load JFxBorderlessNative.dll");
        }
        if (sourceLocation != null) {
            if (targetFolderNullable == null) {
                targetFolderNonNull = new File(System.getProperty("java.io.tmpdir") + "/" + sourceLocation);
            } else {
                targetFolderNonNull = new File(targetFolderNullable);
            }
            File targetLocation = new File(targetFolderNonNull + "/" + dllLocation);
            File parentDirectory = targetLocation.getParentFile();
            if (!parentDirectory.exists()) {
                if (!parentDirectory.mkdirs()) {
                    System.err.println("Failed to create " + parentDirectory.getAbsolutePath());
                }
            }
            if (!targetLocation.exists()) {
                try (InputStream in = BorderlessNative.class.getResourceAsStream(sourceLocation)) {
                    if (in != null) {
                        try (FileOutputStream fos = new FileOutputStream(targetLocation)) {
                            byte[] buffer = new byte[1024];
                            int read;
                            while ((read = in.read(buffer)) != -1) {
                                fos.write(buffer, 0, read);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
            System.load(targetLocation.getAbsolutePath());
        }
    }

    private Node maximizeNode;

    private Node[] captions;

    private final Stage stage;

    private Rectangle2D previousBound = null;

    /**
     * Instantiates a new Borderless native.
     *
     * @param stage the stage
     */
    public BorderlessNative(Stage stage) {
        this.stage = stage;
        //Ensure the layout is recomputed when exiting aero snap region
        this.stage.xProperty().addListener(updateLayout(stage));
        this.stage.yProperty().addListener(updateLayout(stage));
        this.stage.widthProperty().addListener(updateLayout(stage));
        this.stage.heightProperty().addListener(updateLayout(stage));
    }

    private ChangeListener<Number> updateLayout(Stage stage) {
        return (observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                Parent root = stage.getScene().getRoot();
                root.requestLayout();
            }
        };
    }

    /**
     * Sets caption node.
     *
     * @param captions the caption node
     */
    public void setCaptionNode(Node... captions) {
        this.captions = captions;
    }

    /**
     * Sets maximize node.
     *
     * @param maximizeNode the maximize node
     */
    @SuppressWarnings("unused") //Will be available later
    public void setMaximizeNode(Node maximizeNode) {
        this.maximizeNode = maximizeNode;
    }

    /**
     * Make windows borderless.
     *
     * @param windowName the window name
     */
    public native void makeWindowsBorderless(final String windowName);

    /**
     * Sets window draggable.
     *
     * @param isDraggable the is draggable
     */
    @SuppressWarnings("unused") // false positive JNI Method called from native
    public native void setWindowDraggable(boolean isDraggable);

    /**
     * Perce of 2 rect double.
     *
     * @param r1 the r 1
     * @param r2 the r 2
     * @return the double
     */
    public double perceOf2Rect(Rectangle2D r1, Rectangle2D r2) {
        java.awt.geom.Rectangle2D r1a = new java.awt.geom.Rectangle2D.Double(r1.getMinX(), r1.getMinY(), r1.getWidth(), r1.getHeight());
        java.awt.geom.Rectangle2D r1b = new java.awt.geom.Rectangle2D.Double(r2.getMinX(), r2.getMinY(), r2.getWidth(), r2.getHeight());
        return perceOf2Rect(r1a, r1b);

    }

    /**
     * Perce of 2 rect double.
     *
     * @param r1 the r 1
     * @param r2 the r 2
     * @return the double
     */
    public double perceOf2Rect(java.awt.geom.Rectangle2D r1, java.awt.geom.Rectangle2D r2) {
        java.awt.geom.Rectangle2D r = new java.awt.geom.Rectangle2D.Double();
        java.awt.geom.Rectangle2D.intersect(r1, r2, r);

        double fr1 = r1.getWidth() * r1.getHeight();                // area of "r1"
        double f = r.getWidth() * r.getHeight();                  // area of "r" - overlap
        return (fr1 == 0 || f <= 0) ? 0 : (f / fr1) * 100;          // overlap percentage
    }

    /**
     * Maximize or restore.
     */
    public void maximizeOrRestore() {
        stage.setMaximized(false);
        Rectangle2D currentBound = getStageBound();
        Screen selectedScreen = getSelectedScreen(currentBound);
        if (isMaximized(selectedScreen, currentBound)) {
            if(previousBound!=null) {
                stage.setX(previousBound.getMinX());
                stage.setY(previousBound.getMinY());
                stage.setWidth(previousBound.getWidth());
                stage.setHeight(previousBound.getHeight());
            }
            previousBound = null;
        } else {
            Rectangle2D primaryScreenBounds = selectedScreen.getVisualBounds();
            previousBound = currentBound;
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        }
    }

    private Rectangle2D getStageBound() {
        return new Rectangle2D(stage.getX(), stage.getY(),
                stage.getWidth(), stage.getHeight());
    }

    private Screen getSelectedScreen(Rectangle2D currentBound) {
        ObservableList<Screen> screens = Screen.getScreensForRectangle(currentBound);
        Screen selectedScreen = null;
        if (screens.isEmpty()) {
            selectedScreen = Screen.getPrimary();
        } else {
            double maxPercentage = 0;
            for (Screen screen : screens) {
                double newPercentage = perceOf2Rect(screen.getBounds(), currentBound);
                if (newPercentage > maxPercentage) {
                    maxPercentage = newPercentage;
                    selectedScreen = screen;
                }
            }
        }
        return selectedScreen;
    }

    /**
     * Is maximized boolean.
     *
     * @param screen       the screen
     * @param currentBound the current bound
     * @return the boolean
     */
    public boolean isMaximized(Screen screen, Rectangle2D currentBound) {
        Rectangle2D primaryScreenBounds = screen.getVisualBounds();
        return primaryScreenBounds.equals(currentBound);
    }

    /**
     * Pick screen node.
     *
     * @param node    the node
     * @param screenX the screen x
     * @param screenY the screen y
     * @return the node
     */
    public static Node getTopNodeUnderMouse(Node node, double screenX, double screenY) {
        Bounds bounds = node.localToScreen(node.getLayoutBounds());
        // check if the given node has the point inside it, or else we drop out
        if (!bounds.contains(screenX, screenY)) return null;

        Node result = node;
        if (node instanceof Parent) {
            List<Node> children = ((Parent) node).getChildrenUnmodifiable();
            for (Node child : children) {
                Bounds childBounds = child.localToScreen(child.getLayoutBounds());
                if (child.isVisible() && !child.isMouseTransparent() && childBounds.contains(screenX, screenY)) {
                    Node subChild = getTopNodeUnderMouse(child, screenX, screenY);
                    if (subChild != null) {
                        result = subChild;
                    } else {
                        result = child;
                    }
                }
            }
        }
        return result;
    }


    /**
     * Is mouse in caption boolean.
     *
     * @param x the x
     * @param y the y
     * @return the boolean
     */
    @SuppressWarnings("unused") // false positive JNI Method called from native
    public boolean isMouseInCaption(int x, int y) {
        if (captions != null) {
            for (Node captionNode : captions) {
                Node pickedNode = getTopNodeUnderMouse(captionNode, x, y);
                String pickedId = pickedNode != null ? pickedNode.getId() : null;
                boolean inCaption = captionNode.equals(pickedNode);
                if (inCaption) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Is mouse in maximize button boolean.
     *
     * @param x the x
     * @param y the y
     * @return the boolean
     */
    @SuppressWarnings("unused") // false positive JNI Method called from native
    public boolean isMouseInMaximizeButton(int x, int y) {
        boolean result;
        if (maximizeNode != null) {
            Bounds childBounds = maximizeNode.localToScreen(maximizeNode.getLayoutBounds());
            result= childBounds.contains(x,y);
        } else {
            result= false;
        }
        return result;
    }
}
