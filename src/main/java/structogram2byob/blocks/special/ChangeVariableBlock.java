package structogram2byob.blocks.special;

import java.util.List;

import scratchlib.objects.fixed.collections.ScratchObjectArray;
import scratchlib.objects.fixed.data.ScratchObjectSymbol;
import scratchlib.objects.fixed.data.ScratchObjectUtf8;
import structogram2byob.ScratchType;
import structogram2byob.blocks.Block;
import structogram2byob.blocks.BlockDescription;
import structogram2byob.blocks.BlockRegistry;
import structogram2byob.program.ScratchConversionException;
import structogram2byob.program.VariableContext;
import structogram2byob.program.VariableMap;
import structogram2byob.program.expressions.Expression;


/**
 * This is the "change (variable) by (number)" block.
 */
public class ChangeVariableBlock extends Block
{
    private static final BlockDescription description = new BlockDescription.Builder()
            .label("change").param(ScratchType.ANY).label("by").param(ScratchType.NUMBER).build();

    /**
     * The singleton instance.
     */
    public static final ChangeVariableBlock instance = new ChangeVariableBlock();

    private ChangeVariableBlock()
    {
        super(description, null);
    }

    @Override
    public ScratchObjectArray toScratch(List<Expression> params, VariableMap vars, BlockRegistry blocks)
            throws ScratchConversionException
    {
        String name = asVariableName(params.get(0));
        VariableContext context = vars.get(name);

        ScratchObjectArray a = new ScratchObjectArray();

        a.add(new ScratchObjectSymbol(context.requiresBYOB() ? "changeBlockVariable" : "changeVariable"));
        a.add(new ScratchObjectUtf8(name));
        a.add(new ScratchObjectSymbol("changeVar:by:"));

        if (context.requiresBYOB()) {
            a.add(context.getWriteMarker());
        }

        a.add(params.get(1).toScratch(vars, blocks));

        return a;
    }
}
