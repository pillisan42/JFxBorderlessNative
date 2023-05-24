package fr.pilli;

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

public class BorderlessNative {

    public static void loadJarDll(String name, String targetFile) {
        File dllToCopy;

        if (targetFile == null) {
            String[] split = name.split("/");
            String simpleName = split[split.length - 1];
            //dllToCopy = File.createTempFile("abc", simpleName);
            dllToCopy = new File(System.getProperty("java.io.tmpdir") + "/" + name);
            System.out.println("simpleName: " + simpleName + " absolutePath: " + dllToCopy.getAbsolutePath());

        } else {
            dllToCopy = new File(targetFile);

        }
        File parentDirectory = dllToCopy.getParentFile();
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }
        if (!dllToCopy.exists()) {

            try (InputStream in = BorderlessNative.class.getResourceAsStream(name)) {
                try (FileOutputStream fos = new FileOutputStream(dllToCopy)) {
                    byte[] buffer = new byte[1024];
                    int read = -1;
                    while ((read = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        System.load(dllToCopy.getAbsolutePath());
    }

    private Node maximizeNode;

    private Node captionNode;

    private final Stage stage;

    private Rectangle2D previousBound = null;

    public BorderlessNative(Stage stage) {
        this.stage = stage;
    }

    public void setCaptionNode(Node captionNode) {
        this.captionNode = captionNode;
    }

    public void setMaximizeNode(Node maximizeNode) {
        this.maximizeNode = maximizeNode;
    }

    public native void makeWindowsBorderless(final String windowName);

    public native void setWindowDraggable(boolean isDraggable);

    public double perceOf2Rect(Rectangle2D r1, Rectangle2D r2){
        java.awt.geom.Rectangle2D r1a = new java.awt.geom.Rectangle2D.Double(r1.getMinX(),r1.getMinY(),r1.getWidth(),r1.getHeight());
        java.awt.geom.Rectangle2D r1b = new java.awt.geom.Rectangle2D.Double(r2.getMinX(),r2.getMinY(),r2.getWidth(),r2.getHeight());
        return perceOf2Rect(r1a,r1b);

    }

    public double perceOf2Rect(java.awt.geom.Rectangle2D r1, java.awt.geom.Rectangle2D r2){
        java.awt.geom.Rectangle2D r = new java.awt.geom.Rectangle2D.Double();
        java.awt.geom.Rectangle2D.intersect(r1, r2, r);

        double fr1 = r1.getWidth() * r1.getHeight();                // area of "r1"
        double f   = r.getWidth() * r.getHeight();                  // area of "r" - overlap
        return (fr1 == 0 || f <= 0) ? 0 : (f / fr1) * 100;          // overlap percentage
    }

    public void maximizeOrRestore() {
        Rectangle2D currentBound = getStageBound();
        Screen selectedScreen = getSelectedScreen(currentBound);
        if(isMaximized(selectedScreen,currentBound)) {
            stage.setX(previousBound.getMinX());
            stage.setY(previousBound.getMinY());
            stage.setWidth(previousBound.getWidth());
            stage.setHeight(previousBound.getHeight());
            previousBound = null;
        } else {
            Rectangle2D primaryScreenBounds = selectedScreen.getVisualBounds();
            previousBound = currentBound;
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        }
        /*if (previousBound == null) {
            Rectangle2D currentBound = getStageBound();
            Screen selectedScreen = getSelectedScreen();
            Rectangle2D primaryScreenBounds = selectedScreen.getVisualBounds();
            previousBound = currentBound;
            stage.setX(primaryScreenBounds.getMinX());
            stage.setY(primaryScreenBounds.getMinY());
            stage.setWidth(primaryScreenBounds.getWidth());
            stage.setHeight(primaryScreenBounds.getHeight());
        } else {
            stage.setX(previousBound.getMinX());
            stage.setY(previousBound.getMinY());
            stage.setWidth(previousBound.getWidth());
            stage.setHeight(previousBound.getHeight());
            previousBound = null;
        }*/
    }

    private Rectangle2D getStageBound() {
        return new Rectangle2D(stage.getX(), stage.getY(),
                stage.getWidth(), stage.getHeight());
    }

    private Screen getSelectedScreen(Rectangle2D currentBound) {
        ObservableList<Screen> screens=Screen.getScreensForRectangle(currentBound);
        Screen selectedScreen=null;
        if(screens.isEmpty()) {
            selectedScreen=Screen.getPrimary();
        } else {
            double maxPercentage=0;
            for(Screen screen:screens) {
                double newPercentage=perceOf2Rect(screen.getBounds(),currentBound);
                if(newPercentage>maxPercentage) {
                    maxPercentage=newPercentage;
                    selectedScreen=screen;
                }
            }
        }
        return selectedScreen;
    }

    public boolean isMaximized(Screen screen,Rectangle2D currentBound) {
        boolean result=false;
        Rectangle2D primaryScreenBounds = screen.getVisualBounds();
        result=primaryScreenBounds.equals(currentBound);
        return result;
    }

    public static Node pickScreen(Node node, double screenX, double screenY) {
        Bounds bounds = node.localToScreen(node.getLayoutBounds());
        //Point2D p = node.sceneToLocal(sceneX, sceneY, true /* rootScene */);

        // check if the given node has the point inside it, or else we drop out
        if (!bounds.contains(screenX, screenY)) return null;

        // at this point we know that _at least_ the given node is a valid
        // answer to the given point, so we will return that if we don't find
        // a better child option
        if (node instanceof Parent) {
            // we iterate through all children in reverse order, and stop when we find a match.
            // We do this as we know the elements at the end of the list have a higher
            // z-order, and are therefore the better match, compared to children that
            // might also intersect (but that would be underneath the element).
            Node bestMatchingChild = null;
            List<Node> children = ((Parent) node).getChildrenUnmodifiable();

            for (int i = children.size() - 1; i >= 0; i--) {
                Node child = children.get(i);
                Bounds childBounds = child.localToScreen(node.getLayoutBounds());

                if (child.isVisible() && !child.isMouseTransparent() && childBounds.contains(screenX, screenY)) {
                    bestMatchingChild = child;
                    break;
                }
            }

            if (bestMatchingChild != null) {
                return pickScreen(bestMatchingChild, screenX, screenY);
            }
        }

        return node;
    }

    public boolean isMouseInCaption(int x, int y) {
        //System.out.println("isMouseInCaption() x: "+x+", y:"+y+", isCaptionPressed: "+isCaptionPressed.get());
        if (captionNode != null) {
            Node pickedNode = pickScreen(captionNode, x, y);
            return captionNode.equals(pickedNode);
        } else {
            return false;
        }
    }

    public boolean isMouseInMaximizeButton(int x, int y) {
        //System.out.println("isMouseInCaption() x: "+x+", y:"+y+", isCaptionPressed: "+isCaptionPressed.get());
        if (maximizeNode != null) {
            Node pickedNode = pickScreen(maximizeNode, x, y);
            return maximizeNode.equals(pickedNode);
        } else {
            return false;
        }
    }
}
