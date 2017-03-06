package structogram2byob.parser.nsd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nsdlib.elements.NSDContainer;
import nsdlib.elements.NSDElement;
import nsdlib.elements.NSDInstruction;
import nsdlib.elements.NSDRoot;
import nsdlib.elements.alternatives.NSDDecision;
import nsdlib.elements.loops.NSDForever;
import nsdlib.elements.loops.NSDTestFirstLoop;
import structogram2byob.blocks.BlockDescription;
import structogram2byob.blocks.structures.ForeverBlock;
import structogram2byob.blocks.structures.IfBlock;
import structogram2byob.blocks.structures.IfElseBlock;
import structogram2byob.blocks.structures.RepeatBlock;
import structogram2byob.parser.blockdescription.BlockDescriptionParser;
import structogram2byob.parser.blockdescription.BlockDescriptionParserException;
import structogram2byob.parser.expression.ExpressionParser;
import structogram2byob.parser.expression.ExpressionParserException;
import structogram2byob.program.ProgramUnit;
import structogram2byob.program.UnitType;
import structogram2byob.program.expressions.BlockExpression;
import structogram2byob.program.expressions.Expression;
import structogram2byob.program.expressions.ScriptExpression;


/**
 * Parser for constructing {@link ProgramUnit} instances from Nassi-Shneiderman
 * Diagram elements.
 */
public class NSDParser
{
    private final NSDRoot nsd;

    /**
     * Constructs a new parser from the given root element.
     * 
     * @param nsd The element.
     */
    public NSDParser(NSDRoot nsd)
    {
        this.nsd = nsd;
    }

    /**
     * Parses the input NSD as a unit and returns it.
     * 
     * @return The unit that was parsed.
     * 
     * @throws NSDParserException
     */
    public ProgramUnit parse() throws NSDParserException
    {
        String label = nsd.getLabel().trim();

        UnitType type = getUnitType(label);
        if (type != UnitType.SCRIPT) {
            label = label.substring(type.name().length()).trim();
        }

        BlockDescription desc = parseUnitDescription(label);

        List<BlockExpression> blocks = new ArrayList<>();
        for (NSDElement child : nsd) {
            blocks.add(parseElement(child));
        }

        return new ProgramUnit(type, desc, blocks);
    }

    /**
     * Given a unit label, returns the appropriate unit type, defaulting to
     * {@code SCRIPT}. This is determined by the label's prefix (e.g.
     * {@code "REPORTER fact (n)"} would result in {@code REPORTER}).
     * 
     * @param label The label to find the type for.
     * @return The appropriate unit type.
     */
    private UnitType getUnitType(String label)
    {
        UnitType type = UnitType.SCRIPT;

        String labelUc = label.toUpperCase();
        if (labelUc.startsWith("COMMAND ")) {
            type = UnitType.COMMAND;
        } else if (labelUc.startsWith("REPORTER ")) {
            type = UnitType.REPORTER;
        } else if (labelUc.startsWith("PREDICATE ")) {
            type = UnitType.PREDICATE;
        }

        return type;
    }

    /**
     * Parses the given label as a block description.
     * 
     * @param label The label to parse.
     * @return The block description.
     * 
     * @throws NSDParserException If the label could not be parsed.
     */
    private BlockDescription parseUnitDescription(String label)
            throws NSDParserException
    {
        BlockDescriptionParser bdp = new BlockDescriptionParser(label, true);
        try {
            return bdp.parse();
        } catch (BlockDescriptionParserException e) {
            throw new NSDParserException(nsd, "invalid block label", e);
        }
    }

    /**
     * Utility function for parsing an expression string.
     * 
     * @param source The element the expression belongs to.
     * @param exp The expression string.
     * @return The parsed block expression.
     * 
     * @throws NSDParserException
     */
    private Expression parseExpression(NSDElement source, String exp)
            throws NSDParserException
    {
        try {
            return new ExpressionParser(exp).parse();
        } catch (ExpressionParserException e) {
            throw new NSDParserException(source, "expression error", e);
        }
    }

    /**
     * Parses the element according to its type.
     * 
     * @param e The element to parse.
     * @return The parsed block expression.
     * 
     * @throws NSDParserException
     */
    private BlockExpression parseElement(NSDElement e) throws NSDParserException
    {
        if (e instanceof NSDInstruction) {
            return parseInstruction((NSDInstruction) e);
        } else if (e instanceof NSDDecision) {
            return parseDecision((NSDDecision) e);
        } else if (e instanceof NSDForever) {
            return parseForever((NSDForever) e);
        } else if (e instanceof NSDTestFirstLoop) {
            return parseTestFirstLoop((NSDTestFirstLoop) e);
        }

        throw new NSDParserException(e, "unknown element: " + e.getClass());
    }

    /**
     * Parses the given "instruction" element.
     * 
     * @param e The element to parse.
     * @return The parsed block expression.
     * 
     * @throws NSDParserException
     */
    private BlockExpression parseInstruction(NSDInstruction e)
            throws NSDParserException
    {
        return (BlockExpression) parseExpression(e, e.getLabel());
    }

    /**
     * Parses the given "decision" element (i.e., an if or if-else).
     * 
     * @param e The element to parse.
     * @return The parsed block expression.
     * 
     * @throws NSDParserException
     */
    private BlockExpression parseDecision(NSDDecision e)
            throws NSDParserException
    {
        Expression condition = parseExpression(e, e.getLabel());

        ScriptExpression then = parseScript(e.getThen());
        ScriptExpression otherwise = parseScript(e.getElse());

        if (otherwise.size() == 0) {
            return new BlockExpression(IfBlock.instance.getDescription(),
                    Arrays.asList(condition, then));
        }

        return new BlockExpression(IfElseBlock.instance.getDescription(),
                Arrays.asList(condition, then, otherwise));
    }

    /**
     * Parses the given "forever" loop element.
     * 
     * @param e The element to parse.
     * @return The parsed block expression.
     * 
     * @throws NSDParserException
     */
    private BlockExpression parseForever(NSDForever e) throws NSDParserException
    {
        return new BlockExpression(ForeverBlock.instance.getDescription(),
                Arrays.asList(parseScript(e)));
    }

    /**
     * Parses the given "test first" loop element.
     * 
     * @param e The element to parse.
     * @return The parsed block expression.
     * 
     * @throws NSDParserException
     */
    private BlockExpression parseTestFirstLoop(NSDTestFirstLoop e)
            throws NSDParserException
    {
        ScriptExpression content = parseScript(e);

        String label = e.getLabel(), lcLabel = label.toLowerCase();

        if (lcLabel.startsWith("repeat")) {

            String countLabel = label.substring("repeat".length()).trim();
            Expression count = parseExpression(e, countLabel);

            return new BlockExpression(RepeatBlock.instance.getDescription(),
                    Arrays.asList(count, content));

        }

        throw new NSDParserException(e, "unknown loop type");
    }

    /**
     * Parses all child elements of the given container and returns them as a
     * script expression.
     * 
     * @param e The container element.
     * @return The parsed script expression.
     * 
     * @throws NSDParserException
     */
    private ScriptExpression parseScript(NSDContainer<NSDElement> e)
            throws NSDParserException
    {
        List<BlockExpression> blocks = new ArrayList<>();

        for (NSDElement child : e) {
            blocks.add(parseElement(child));
        }

        return new ScriptExpression(blocks);
    }
}