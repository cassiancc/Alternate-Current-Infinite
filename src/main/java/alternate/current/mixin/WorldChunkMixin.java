package alternate.current.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import alternate.current.Wire;
import alternate.current.interfaces.mixin.IChunk;
import alternate.current.interfaces.mixin.IChunkSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class WorldChunkMixin implements IChunk {
	
	@Shadow @Final private ChunkSection[] sections;
	
	@Override
	public void clearWires() {
		for (ChunkSection section : sections) {
			if (section == null || ChunkSection.isEmpty(section)) {
				continue;
			}
			
			((IChunkSection)section).clearWires();
		}
	}
	
	@Override
	public Wire getWireV2(BlockPos pos) {
		ChunkSection section = getSection(pos.getY());
		
		if (section == null || ChunkSection.isEmpty(section)) {
			return null;
		}
		
		int x = pos.getX() & 15;
		int y = pos.getY() & 15;
		int z = pos.getZ() & 15;
		
		return ((IChunkSection)section).getWireV2(x, y, z);
	}
	
	@Override
	public Wire setWireV2(BlockPos pos, Wire wire) {
		ChunkSection section = getSection(pos.getY());
		
		if (ChunkSection.isEmpty(section)) {
			return null;
		}
		
		int x = pos.getX() & 15;
		int y = pos.getY() & 15;
		int z = pos.getZ() & 15;
		
		return ((IChunkSection)section).setWireV2(x, y, z, wire);
	}
	
	private ChunkSection getSection(int y) {
		if (y < 0) {
			return null;
		}
		
		int index = y >> 4;
		
		if (index >= sections.length) {
			return null;
		}
		
		ChunkSection section = sections[index];
		
		if (ChunkSection.isEmpty(section)) {
			return null;
		}
		
		return section;
	}
}