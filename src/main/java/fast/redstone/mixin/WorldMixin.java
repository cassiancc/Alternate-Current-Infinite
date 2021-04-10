package fast.redstone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fast.redstone.interfaces.mixin.IChunk;
import fast.redstone.interfaces.mixin.IWorld;
import fast.redstone.v1.WireConnectionV1;
import fast.redstone.v1.WireV1;
import fast.redstone.v2.WireV2;

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
	public WireV1 getWireV1(BlockPos pos) {
		if (isClient() || isDebugWorld()) {
			return null;
		}
		
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		
		return ((IChunk)getChunk(x, z)).getWireV1(pos);
	}
	
	@Override
	public void setWireV1(BlockPos pos, WireV1 wire, boolean updateConnections) {
		if (isClient() || isDebugWorld()) {
			return;
		}
		
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		
		WireV1 oldWire = ((IChunk)getChunk(x, z)).setWireV1(pos, wire);
		
		if (updateConnections) {
			if (oldWire != null) {
				oldWire.remove();
				
				for (WireConnectionV1 connection : oldWire.getConnections()) {
					connection.wire.updateConnections();
				}
			}
			if (wire != null) {
				wire.updateConnections();
			}
		}
	}
	
	@Override
	public WireV2 getWireV2(BlockPos pos) {
		if (isClient() || isDebugWorld()) {
			return null;
		}
		
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		
		return ((IChunk)getChunk(x, z)).getWireV2(pos);
	}
	
	@Override
	public void setWireV2(BlockPos pos, WireV2 wire, boolean updateConnections) {
		if (isClient() || isDebugWorld()) {
			return;
		}
		
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		
		WireV2 oldWire = ((IChunk)getChunk(x, z)).setWireV2(pos, wire);
		
		if (updateConnections) {
			if (oldWire != null) {
				oldWire.remove();
				
				for (WireV2 connectedWire : oldWire.getConnectionsOut()) {
					connectedWire.updateConnections();
				}
				for (WireV2 connectedWire : oldWire.getConnectionsIn()) {
					connectedWire.updateConnections();
				}
			}
			if (wire != null) {
				wire.updateConnections();
			}
		}
	}
}

