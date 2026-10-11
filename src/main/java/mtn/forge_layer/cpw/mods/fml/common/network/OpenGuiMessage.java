package mtn.forge_layer.cpw.mods.fml.common.network;

import mtn.forge_layer.cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Stand-in for {@code cpw.mods.fml.common.network.internal.FMLNetworkHandler} +
 * the OpenGui packet. The message that goes server → client telling it to open
 * a GUI with a given id and block position.
 */
public class OpenGuiMessage implements IMessage {

    private int syncId;
    private int guiId;
    private int x;
    private int y;
    private int z;

    public OpenGuiMessage() {
    }

    public OpenGuiMessage(int syncId, int guiId, int x, int y, int z) {
        this.syncId = syncId;
        this.guiId = guiId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(net.minecraft.network.FriendlyByteBuf buf) {
        syncId = buf.readInt();
        guiId = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeInt(guiId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public int getSyncId() {
        return syncId;
    }

    public int getGuiId() {
        return guiId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
