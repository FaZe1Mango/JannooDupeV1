
package de.jannoo.dupe;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class JannooDupeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) ->
                dispatcher.register(
                    ClientCommandManager.literal("jannoo-dupe")
                        .then(ClientCommandManager.argument(
                            "amount",
                            IntegerArgumentType.integer(1, 2304)
                        ).executes(context -> {

                            Minecraft mc = Minecraft.getInstance();

                            if (mc.player == null) return 0;

                            MinecraftServer server = mc.getMultiplayerServer();

                            if (server == null) {
                                mc.player.displayClientMessage(
                                    Component.literal("Nur im Singleplayer!"),
                                    false
                                );
                                return 0;
                            }

                            int amount = IntegerArgumentType.getInteger(
                                context, "amount"
                            );

                            var uuid = mc.player.getUUID();

                            server.execute(() -> {
                                ServerPlayer player =
                                    server.getPlayerList().getPlayer(uuid);

                                if (player == null) return;

                                ItemStack original = player.getMainHandItem();

                                if (original.isEmpty()) return;

                                ItemStack copy = original.copy();
                                copy.setCount(amount);

                                while (!copy.isEmpty()) {
                                    int size = Math.min(
                                        copy.getCount(),
                                        copy.getMaxStackSize()
                                    );

                                    ItemStack stack = copy.split(size);

                                    if (!player.getInventory().add(stack)) {
                                        player.drop(stack, false);
                                    }
                                }

                                player.containerMenu.broadcastChanges();

                                player.displayClientMessage(
                                    Component.literal(
                                        "Jannoo Dupe: " + amount + " Items!"
                                    ),
                                    false
                                );
                            });

                            return 1;
                        })
                    )
                )
        );
    }
}
