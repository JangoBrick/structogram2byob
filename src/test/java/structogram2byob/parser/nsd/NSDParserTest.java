package structogram2byob.parser.nsd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import nsdlib.elements.NSDInstruction;
import nsdlib.elements.NSDRoot;
import nsdlib.elements.alternatives.NSDDecision;
import nsdlib.elements.loops.NSDForever;
import nsdlib.elements.loops.NSDTestFirstLoop;
import structogram2byob.ScratchType;
import structogram2byob.blocks.BlockDescription;
import structogram2byob.blocks.structures.ForeverBlock;
import structogram2byob.blocks.structures.IfBlock;
import structogram2byob.blocks.structures.IfElseBlock;
import structogram2byob.blocks.structures.RepeatBlock;
import structogram2byob.program.ProgramUnit;
import structogram2byob.program.UnitType;
import structogram2byob.program.expressions.BlockExpression;
import structogram2byob.program.expressions.NumberExpression;
import structogram2byob.program.expressions.ScriptExpression;
import structogram2byob.program.expressions.StringExpression;


public class NSDParserTest
{
    @Test
    public void detectsUnitType() throws NSDParserException
    {
        ProgramUnit result;

        result = new NSDParser(new NSDRoot("foo")).parse();
        assertSame(UnitType.SCRIPT, result.getType());

        result = new NSDParser(new NSDRoot("COMMAND foo")).parse();
        assertSame(UnitType.COMMAND, result.getType());

        result = new NSDParser(new NSDRoot("REPORTER foo")).parse();
        assertSame(UnitType.REPORTER, result.getType());

        result = new NSDParser(new NSDRoot("PREDICATE foo")).parse();
        assertSame(UnitType.PREDICATE, result.getType());
    }

    @Test
    public void parsesCustomBlockHeaders() throws NSDParserException
    {
        NSDRoot nsd = new NSDRoot("REPORTER add (a) and (b)");
        ProgramUnit result = new NSDParser(nsd).parse();

        assertEquals("add %a and %b", result.getUserSpec());
    }

    @Test
    public void parsesInstructions() throws NSDParserException
    {
        NSDRoot nsd = new NSDRoot("REPORTER add (a) and (b)");
        nsd.addChild(new NSDInstruction("say \"adding now!\""));
        nsd.addChild(new NSDInstruction("report ((a) + (b))"));

        ProgramUnit result = new NSDParser(nsd).parse();

        List<BlockExpression> blocks = result.getBlocks();

        assertEquals(2, blocks.size());

        BlockExpression block0 = blocks.get(0);
        assertEquals(
                new BlockDescription.Builder().label("say")
                        .param(ScratchType.TEXT).build(),
                block0.getDescription());
        assertEquals(1, block0.getParameters().size());
        assertEquals("adding now!",
                ((StringExpression) block0.getParameters().get(0)).getValue());

        BlockExpression block1 = blocks.get(1);
        assertEquals(
                new BlockDescription.Builder().label("report")
                        .param(ScratchType.ANY).build(),
                block1.getDescription());
        assertEquals(1, block1.getParameters().size());

        BlockExpression p0 = (BlockExpression) block1.getParameters().get(0);
        assertEquals(new BlockDescription.Builder().param(ScratchType.ANY)
                .label("+").param(ScratchType.ANY).build(),
                p0.getDescription());
        assertEquals(2, p0.getParameters().size());

        BlockExpression p0p0 = (BlockExpression) p0.getParameters().get(0);
        assertEquals(new BlockDescription.Builder().label("a").build(),
                p0p0.getDescription());
        assertEquals(0, p0p0.getParameters().size());

        BlockExpression p0p1 = (BlockExpression) p0.getParameters().get(1);
        assertEquals(new BlockDescription.Builder().label("b").build(),
                p0p1.getDescription());
        assertEquals(0, p0p1.getParameters().size());
    }

    @Test
    public void parsesIfThen() throws NSDParserException
    {
        NSDRoot nsd = new NSDRoot("COMMAND test");
        nsd.addChild(new NSDDecision("true",
                Arrays.asList(new NSDInstruction("say \"then\""))));

        ProgramUnit result = new NSDParser(nsd).parse();

        List<BlockExpression> blocks = result.getBlocks();

        assertEquals(1, blocks.size());

        BlockExpression block0 = blocks.get(0);
        assertEquals(IfBlock.instance.getDescription(),
                block0.getDescription());
        assertEquals(2, block0.getParameters().size());

        BlockExpression p0 = (BlockExpression) block0.getParameters().get(0);
        assertEquals(new BlockDescription.Builder().label("true").build(),
                p0.getDescription());
        assertEquals(0, p0.getParameters().size());

        ScriptExpression p1 = (ScriptExpression) block0.getParameters().get(1);
        assertEquals(1, p1.size());
    }

    @Test
    public void parsesIfThenElse() throws NSDParserException
    {
        NSDRoot nsd = new NSDRoot("COMMAND test");
        nsd.addChild(new NSDDecision("true",
                Arrays.asList(new NSDInstruction("say \"then\"")),
                Arrays.asList(new NSDInstruction("say \"else\""))));

        ProgramUnit result = new NSDParser(nsd).parse();

        List<BlockExpression> blocks = result.getBlocks();

        assertEquals(1, blocks.size());

        BlockExpression block0 = blocks.get(0);
        assertEquals(IfElseBlock.instance.getDescription(),
                block0.getDescription());
        assertEquals(3, block0.getParameters().size());

        BlockExpression p0 = (BlockExpression) block0.getParameters().get(0);
        assertEquals(new BlockDescription.Builder().label("true").build(),
                p0.getDescription());
        assertEquals(0, p0.getParameters().size());

        ScriptExpression p1 = (ScriptExpression) block0.getParameters().get(1);
        assertEquals(1, p1.size());

        ScriptExpression p2 = (ScriptExpression) block0.getParameters().get(2);
        assertEquals(1, p2.size());
    }

    @Test
    public void parsesForever() throws NSDParserException
    {
        NSDRoot nsd = new NSDRoot("COMMAND test");
        nsd.addChild(new NSDForever(Arrays.asList(//
                new NSDInstruction("say \"on and on\"")//
        )));

        ProgramUnit result = new NSDParser(nsd).parse();

        List<BlockExpression> blocks = result.getBlocks();

        assertEquals(1, blocks.size());

        BlockExpression block0 = blocks.get(0);
        assertEquals(ForeverBlock.instance.getDescription(),
                block0.getDescription());
        assertEquals(1, block0.getParameters().size());

        ScriptExpression p0 = (ScriptExpression) block0.getParameters().get(0);
        assertEquals(1, p0.size());
    }

    @Test
    public void parsesRepeat() throws NSDParserException
    {
        NSDRoot nsd = new NSDRoot("COMMAND test");
        nsd.addChild(new NSDTestFirstLoop("repeat 10", Arrays.asList(//
                new NSDInstruction("say \"looping\"")//
        )));

        ProgramUnit result = new NSDParser(nsd).parse();

        List<BlockExpression> blocks = result.getBlocks();

        assertEquals(1, blocks.size());

        BlockExpression block0 = blocks.get(0);
        assertEquals(RepeatBlock.instance.getDescription(),
                block0.getDescription());
        assertEquals(2, block0.getParameters().size());

        NumberExpression p0 = (NumberExpression) block0.getParameters().get(0);
        assertEquals(10, (int) p0.getValue());

        ScriptExpression p1 = (ScriptExpression) block0.getParameters().get(1);
        assertEquals(1, p1.size());
    }
}