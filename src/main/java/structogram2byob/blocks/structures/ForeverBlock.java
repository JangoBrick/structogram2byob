package structogram2byob.blocks.structures;

import java.util.List;

import scratchlib.objects.fixed.collections.ScratchObjectArray;
import scratchlib.objects.fixed.data.ScratchObjectSymbol;
import structogram2byob.ScratchType;
import structogram2byob.blocks.Block;
import structogram2byob.blocks.BlockDescription;
import structogram2byob.blocks.BlockRegistry;
import structogram2byob.program.ScratchConversionException;
import structogram2byob.program.VariableMap;
import structogram2byob.program.expressions.Expression;


/**
 * This is the "forever" C-shape block.
 */
public class ForeverBlock extends Block
{
    private static final BlockDescription description = new BlockDescription.Builder()
            .label("forever").param(ScratchType.LOOP).build();

    /**
     * The singleton instance.
     */
    public static final ForeverBlock instance = new ForeverBlock();

    private ForeverBlock()
    {
        super(description, null);
    }

    @Override
    public ScratchObjectArray toScratch(List<Expression> params, VariableMap vars, BlockRegistry blocks)
            throws ScratchConversionException
    {
        ScratchObjectArray a = new ScratchObjectArray();

        a.add(new ScratchObjectSymbol("doForever"));

        // blocks
        a.add(params.get(0).toScratch(vars, blocks));

        return a;
    }
}
