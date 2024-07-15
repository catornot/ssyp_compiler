import java.util.ArrayList;


public class Node {
    final private ArrayList<Node> childs;
    final private String id;

    private String parent = null;

    public Node(ArrayList<Node> childs, String id) {
        this.childs = childs;
        for (Node child: childs) {
            child.parent = id;
        }
        this.id = id;
    }

    public NodeType getStatus() {
        if (this.deg() != 0) {
            if (this.getParentId() == null) {
                return NodeType.ROOT;
            }
            return NodeType.NODE;
        } else {
            return NodeType.LEAVES;
        }
    }

    public String getParentId() {
        return this.parent;
    }

    public ArrayList<Node> get_childs() {
        return this.childs;
    }

    public int deg() {
        return this.childs.size();
    }

    public String get_id() {
        return id;
    }
}
