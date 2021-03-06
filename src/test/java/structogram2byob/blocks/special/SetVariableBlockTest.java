package structogram2byob.blocks.special;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import scratchlib.objects.ScratchObject;
import scratchlib.objects.fixed.collections.ScratchObjectArray;
import scratchlib.objects.fixed.data.ScratchObjectSymbol;
import scratchlib.objects.fixed.data.ScratchObjectUtf8;
import scratchlib.objects.inline.ScratchObjectAbstractNumber;
import scratchlib.objects.user.ScratchObjectVariableFrame;
import structogram2byob.ScratchType;
import structogram2byob.blocks.BlockDescription;
import structogram2byob.blocks.BlockRegistry;
import structogram2byob.program.ProgramUnit;
import structogram2byob.program.ScratchConversionException;
import structogram2byob.program.UnitType;
import structogram2byob.program.VariableContext;
import structogram2byob.program.VariableMap;
import structogram2byob.program.expressions.BlockExpression;
import structogram2byob.program.expressions.Expression;
import structogram2byob.program.expressions.NumberExpression;

import static org.junit.jupiter.api.Assertions.*;


public class SetVariableBlockTest
{
    @Test
    public void convertsToScratch() throws ScratchConversionException
    {
        SetVariableBlock obj = SetVariableBlock.instance;

        List<Expression> params = Arrays.asList(
                new BlockExpression(null, new BlockDescription.Builder().label("foobar").build(),
                        Collections.emptyList()),
                new NumberExpression(null, 42)
        );
        VariableMap vars;
        BlockRegistry blocks = new BlockRegistry();

        // global

        vars = new VariableMap(Collections.singletonMap("foobar", VariableContext.getGlobal()));
        ScratchObjectArray scratch = obj.toScratch(params, vars, blocks);

        assertEquals(4, scratch.size());

        assertEquals("changeVariable", ((ScratchObjectSymbol) scratch.get(0)).getValue());
        assertEquals("foobar", ((ScratchObjectUtf8) scratch.get(1)).getValue());
        assertEquals("setVar:to:", ((ScratchObjectSymbol) scratch.get(2)).getValue());
        assertEquals(42, ((ScratchObjectAbstractNumber) scratch.get(3)).intValue());

        // unit specific (parameter)

        ProgramUnit unit = new ProgramUnit(null, UnitType.COMMAND,
                new BlockDescription.Builder().label("doSomething").param(ScratchType.ANY, "foobar").build(),
                Collections.emptyList());
        vars = new VariableMap(Collections.singletonMap("foobar", VariableContext.getForUnit(unit)));
        scratch = obj.toScratch(params, vars, blocks);

        assertEquals(5, scratch.size());

        assertEquals("changeBlockVariable", ((ScratchObjectSymbol) scratch.get(0)).getValue());
        assertEquals("foobar", ((ScratchObjectUtf8) scratch.get(1)).getValue());
        assertEquals("setVar:to:", ((ScratchObjectSymbol) scratch.get(2)).getValue());
        assertSame(ScratchObject.NIL, scratch.get(3));
        assertEquals(42, ((ScratchObjectAbstractNumber) scratch.get(4)).intValue());

        // script specific (script variables)

        ScratchObjectVariableFrame frame = new ScratchObjectVariableFrame();
        vars = new VariableMap(Collections.singletonMap("foobar", VariableContext.getForScript(frame)));
        scratch = obj.toScratch(params, vars, blocks);

        assertEquals(5, scratch.size());

        assertEquals("changeBlockVariable", ((ScratchObjectSymbol) scratch.get(0)).getValue());
        assertEquals("foobar", ((ScratchObjectUtf8) scratch.get(1)).getValue());
        assertEquals("setVar:to:", ((ScratchObjectSymbol) scratch.get(2)).getValue());
        assertSame(frame, scratch.get(3));
        assertEquals(42, ((ScratchObjectAbstractNumber) scratch.get(4)).intValue());
    }
}
