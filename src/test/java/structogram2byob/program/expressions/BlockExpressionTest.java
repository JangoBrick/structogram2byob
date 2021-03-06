package structogram2byob.program.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import scratchlib.objects.fixed.collections.ScratchObjectArray;
import scratchlib.objects.fixed.data.ScratchObjectString;
import scratchlib.objects.fixed.data.ScratchObjectSymbol;
import scratchlib.objects.fixed.data.ScratchObjectUtf8;
import scratchlib.objects.user.ScratchObjectVariableFrame;
import structogram2byob.ScratchType;
import structogram2byob.blocks.Block;
import structogram2byob.blocks.BlockDescription;
import structogram2byob.blocks.BlockRegistry;
import structogram2byob.blocks.FunctionBlock;
import structogram2byob.program.ProgramUnit;
import structogram2byob.program.ScratchConversionException;
import structogram2byob.program.UnitType;
import structogram2byob.program.VariableContext;
import structogram2byob.program.VariableMap;

import static org.junit.jupiter.api.Assertions.*;


public class BlockExpressionTest
{
    private static final BlockDescription DESC = new BlockDescription.Builder()
            .label("move").param(ScratchType.NUMBER).label("steps").build();

    private static final BlockDescription DESC_VAR = new BlockDescription.Builder()
            .label("foobar").build();

    @Test
    public void returnsDescription()
    {
        BlockExpression obj = new BlockExpression(null, DESC,
                Collections.singletonList(new NumberExpression(null, 42)));

        assertSame(DESC, obj.getDescription());
    }

    @Test
    public void returnsUnmodifiableParametersList()
    {
        List<Expression> params = new ArrayList<>();
        params.add(new NumberExpression(null, 42));

        BlockExpression obj = new BlockExpression(null, DESC, params);

        assertEquals(params, obj.getParameters());

        assertThrows(UnsupportedOperationException.class, () -> {
            obj.getParameters().set(0, new NumberExpression(null, 37));
        });
    }

    @Test
    public void returnsCorrectType()
    {
        BlockExpression obj = new BlockExpression(null, DESC,
                Collections.singletonList(new NumberExpression(null, 42)));

        assertSame(ScratchType.ANY, obj.getType());
    }

    @Test
    public void convertsFunctionBlockToScratch() throws ScratchConversionException
    {
        BlockRegistry blocks = new BlockRegistry();
        Block block = new FunctionBlock(DESC, null, "forward:");
        blocks.register(block);

        BlockExpression obj = new BlockExpression(null, DESC,
                Collections.singletonList(new NumberExpression(null, 42)));

        ScratchObjectArray blockResult = block.toScratch(
                Collections.singletonList(new NumberExpression(null, 42)), VariableMap.EMPTY, blocks);
        ScratchObjectArray objResult = (ScratchObjectArray) obj.toScratch(VariableMap.EMPTY, blocks);

        assertEquals(blockResult.size(), objResult.size());

        for (int i = 0; i < blockResult.size(); ++i) {
            assertEquals(blockResult.get(i).getClass(), objResult.get(i).getClass());
        }
    }

    @Test
    public void convertsGlobalVariableToScratch() throws ScratchConversionException
    {
        VariableMap vars = new VariableMap(Collections.singletonMap("foobar", VariableContext.getGlobal()));
        BlockRegistry blocks = new BlockRegistry();

        BlockExpression obj = new BlockExpression(null, DESC_VAR, Collections.emptyList());

        ScratchObjectArray result = (ScratchObjectArray) obj.toScratch(vars, blocks);

        assertEquals(2, result.size());
        assertEquals("readVariable", ((ScratchObjectSymbol) result.get(0)).getValue());
        assertEquals("foobar", ((ScratchObjectUtf8) result.get(1)).getValue());
    }

    @Test
    public void convertsUnitVariableToScratch() throws ScratchConversionException
    {
        ProgramUnit unit = new ProgramUnit(null,
                UnitType.COMMAND, // type
                new BlockDescription.Builder().label("doSomething").param(ScratchType.ANY, "foobar").build(), // desc
                Collections.emptyList() // blocks
        );

        VariableMap vars = new VariableMap(Collections.singletonMap("foobar", VariableContext.getForUnit(unit)));
        BlockRegistry blocks = new BlockRegistry();

        BlockExpression obj = new BlockExpression(null, DESC_VAR, Collections.emptyList());

        ScratchObjectArray result = (ScratchObjectArray) obj.toScratch(vars, blocks);

        assertEquals(5, result.size());
        assertEquals("byob", ((ScratchObjectSymbol) result.get(0)).getValue());
        assertEquals("", ((ScratchObjectString) result.get(1)).getValue());
        assertEquals("readBlockVariable", ((ScratchObjectSymbol) result.get(2)).getValue());
        assertEquals("foobar", ((ScratchObjectUtf8) result.get(3)).getValue());
        assertEquals("doSomething %foobar", ((ScratchObjectUtf8) result.get(4)).getValue());
    }

    @Test
    public void convertsScriptVariableToScratch() throws ScratchConversionException
    {
        ScratchObjectVariableFrame frame = new ScratchObjectVariableFrame();
        VariableMap vars = new VariableMap(Collections.singletonMap("foobar", VariableContext.getForScript(frame)));
        BlockRegistry blocks = new BlockRegistry();

        BlockExpression obj = new BlockExpression(null, DESC_VAR, Collections.emptyList());

        ScratchObjectArray result = (ScratchObjectArray) obj.toScratch(vars, blocks);

        assertEquals(5, result.size());
        assertEquals("byob", ((ScratchObjectSymbol) result.get(0)).getValue());
        assertEquals("", ((ScratchObjectString) result.get(1)).getValue());
        assertEquals("readBlockVariable", ((ScratchObjectSymbol) result.get(2)).getValue());
        assertEquals("foobar", ((ScratchObjectUtf8) result.get(3)).getValue());
        assertSame(frame, result.get(4));
    }

    @Test
    public void throwsForUnknownBlock()
    {
        BlockRegistry blocks = new BlockRegistry();

        BlockExpression obj = new BlockExpression(null, DESC,
                Collections.singletonList(new NumberExpression(null, 42)));

        assertThrows(ScratchConversionException.class, () -> obj.toScratch(VariableMap.EMPTY, blocks));
    }

    @Test
    public void convertsToString()
    {
        BlockExpression obj;

        obj = new BlockExpression(null, new BlockDescription.Builder().label("foo").build(), Collections.emptyList());
        assertEquals("(foo)", obj.toString());

        obj = new BlockExpression(null,
                new BlockDescription.Builder().label("foo").param(ScratchType.ANY).label("bar").build(),
                Collections.singletonList(
                        new BlockExpression(null, new BlockDescription.Builder().label("param").build(),
                                Collections.emptyList())
                ));
        assertEquals("(foo (param) bar)", obj.toString());
    }
}
