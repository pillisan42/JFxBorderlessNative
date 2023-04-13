package fr.pilli;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

public class BorderlessNative {

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

    public void maximizeOrRestore() {
        if (previousBound == null) {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            previousBound = new Rectangle2D(stage.getX(), stage.getY(),
                    stage.getWidth(), stage.getHeight());
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
        }
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
