package structogram2byob.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import structogram2byob.lexer.Token;


/**
 * Represents a node in an Abstract Syntax Tree (AST), or the tree itself.
 */
public class AstNode implements Iterable<AstNode>
{
    private final Token value;
    private final List<AstNode> branches;

    /**
     * Constructs a new node with no direct value that can have branches added to it.
     */
    public AstNode()
    {
        this.value = null;
        this.branches = new ArrayList<>();
    }

    /**
     * Constructs a new node with a direct value, but without the capability of
     * having branches added to it.
     *
     * @param value This node's value.
     * @throws NullPointerException If value is null.
     */
    public AstNode(Token value)
    {
        this.value = Objects.requireNonNull(value);
        this.branches = null;
    }

    /**
     * Adds the given node as a branch to this node.
     *
     * @param branch The node to add.
     * @throws UnsupportedOperationException If this is not a branching node.
     */
    public void add(AstNode branch)
    {
        if (branches == null) {
            throw new UnsupportedOperationException("is a leaf");
        }
        branches.add(branch);
    }

    /**
     * @return Whether a direct value is present on this node.
     */
    public boolean hasValue()
    {
        return value != null;
    }

    /**
     * @return This token's value, if one is present.
     */
    public Token getValue()
    {
        return value;
    }

    /**
     * @return The number of branches this node has.
     *
     * @throws UnsupportedOperationException If this is not a branching node.
     */
    public int countBranches()
    {
        if (branches == null) {
            throw new UnsupportedOperationException("is a leaf");
        }
        return branches.size();
    }

    /**
     * Returns the branch at the given index, if this is a branching node.
     *
     * @param index The branch index.
     * @return The branch node at the given index.
     *
     * @throws UnsupportedOperationException If this is not a branching node.
     */
    public AstNode getBranch(int index)
    {
        if (branches == null) {
            throw new UnsupportedOperationException("is a leaf");
        }
        return branches.get(index);
    }

    /**
     * Returns an iterator over this node's branches.
     *
     * @return An iterator over the branches.
     * @throws UnsupportedOperationException If this is not a branching node.
     */
    @Override
    public Iterator<AstNode> iterator()
    {
        if (branches == null) {
            throw new UnsupportedOperationException("is a leaf");
        }
        return branches.iterator();
    }

    @Override
    public String toString()
    {
        if (value != null) {
            return "[" + value + "]";
        }

        assert branches != null;
        return branches.stream().map(AstNode::toString)
                .collect(Collectors.joining(", ", "(", ")"));
    }
}
