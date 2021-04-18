package alternate.current.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import alternate.current.Wire;
import alternate.current.interfaces.mixin.IChunk;
import alternate.current.interfaces.mixin.IWorld;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(World.class)
public abstract class WorldMixin implements IWorld {
	
	private int blockUpdateCount;
	
	@Shadow public abstract WorldChunk getChunk(int x, int z);
	@Shadow public abstract boolean isDebugWorld();
	@Shadow public abstract boolean isClient();
	
	@Inject(
			method = "updateNeighbor",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;neighborUpdate(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V"
			)
	)
	private void onUpdateNeighborInjectAtNeighborUpdate(BlockPos sourcePos, Block sourceBlock, BlockPos neighborPos, CallbackInfo ci) {
		if (!isClient()) {
			blockUpdateCount++;
		}
	}
	
	@Override
	public void reset() {
		blockUpdateCount = 0;
	}
	
	@Override
	public int getCount() {
		return blockUpdateCount;
	}
	
	@Override
	public Wire getWire(BlockPos pos) {
		if (isClient() || isDebugWorld()) {
			return null;
		}
		
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		
		return ((IChunk)getChunk(x, z)).getWireV2(pos);
	}
	
	@Override
	public void setWire(BlockPos pos, Wire wire, boolean updateConnections) {
		if (isClient() || isDebugWorld()) {
			return;
		}
		
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		
		Wire oldWire = ((IChunk)getChunk(x, z)).setWireV2(pos, wire);
		
		if (updateConnections) {
			if (oldWire != null) {
				oldWire.removed();
				
				for (BlockPos neighborPos : oldWire.connectionsOut) {
					Wire connectedWire = getWire(neighborPos);
					
					if (connectedWire != null) {
						connectedWire.updateConnections();
					}
				}
				for (BlockPos neighborPos : oldWire.connectionsIn) {
					Wire connectedWire = getWire(neighborPos);
					
					if (connectedWire != null) {
						connectedWire.updateConnections();
					}
				}
			}
			if (wire != null) {
				wire.updateConnections();
			}
		}
	}
}
