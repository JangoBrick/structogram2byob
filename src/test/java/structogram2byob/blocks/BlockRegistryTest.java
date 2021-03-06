package structogram2byob.blocks;

import java.util.List;

import org.junit.jupiter.api.Test;
import scratchlib.objects.fixed.collections.ScratchObjectArray;
import structogram2byob.ScratchType;
import structogram2byob.program.VariableMap;
import structogram2byob.program.expressions.Expression;

import static org.junit.jupiter.api.Assertions.*;


public class BlockRegistryTest
{
    private static class MockBlock extends Block
    {
        public MockBlock(BlockDescription desc, ScratchType returnType)
        {
            super(desc, returnType);
        }

        @Override
        public ScratchObjectArray toScratch(List<Expression> params, VariableMap vars, BlockRegistry blocks)
        {
            return null;
        }
    }

    @Test
    public void looksUpRegisteredBlocks()
    {
        BlockRegistry obj = new BlockRegistry();

        Block block = new MockBlock(new BlockDescription.Builder().label("foo").param(ScratchType.ANY).build(),
                ScratchType.BOOLEAN);
        obj.register(block);

        Block lookup = obj.lookup(new BlockDescription.Builder().label("foo").param(ScratchType.NUMBER).build());
        assertSame(block, lookup);

        Block unknown = obj.lookup(new BlockDescription.Builder().label("bar").build());
        assertNull(unknown);
    }

    @Test
    public void throwsForDuplicates()
    {
        BlockRegistry obj = new BlockRegistry();

        obj.register(new MockBlock(new BlockDescription.Builder().label("foo").param(ScratchType.ANY).build(),
                ScratchType.BOOLEAN));

        assertThrows(IllegalArgumentException.class, () -> {
            obj.register(new MockBlock(new BlockDescription.Builder().label("foo").param(ScratchType.ANY).build(),
                    ScratchType.BOOLEAN));
        });
    }

    @Test
    public void looksUpFromBase()
    {
        BlockRegistry base = new BlockRegistry();

        Block block = new MockBlock(new BlockDescription.Builder().label("foo").param(ScratchType.ANY).build(),
                ScratchType.BOOLEAN);
        base.register(block);

        BlockRegistry obj = new BlockRegistry(base);

        Block lookup = obj.lookup(new BlockDescription.Builder().label("foo").param(ScratchType.NUMBER).build());
        assertSame(block, lookup);
    }
}
