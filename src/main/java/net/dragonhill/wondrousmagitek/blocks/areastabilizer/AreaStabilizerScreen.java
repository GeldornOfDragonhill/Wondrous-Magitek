package net.dragonhill.wondrousmagitek.blocks.areastabilizer;

import net.dragonhill.wondrousmagitek.global.ScopedState;
import net.dragonhill.wondrousmagitek.network.ModNetworkChannel;
import net.dragonhill.wondrousmagitek.ui.InventoryScreen;
import net.dragonhill.wondrousmagitek.ui.widgets.RangeSelector;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class AreaStabilizerScreen extends InventoryScreen<AreaStabilizerContainer> {
	private RangeSelector rangeSelector;
	private Button button;
	private String radiusHelpMessage = "";

	public AreaStabilizerScreen(final AreaStabilizerContainer container, final PlayerInventory inventory, final ITextComponent title) {
		super(container, inventory, title);

		container.maxRadius.registerCallback(maxRadius -> {
			this.rangeSelector.setMax(maxRadius.get());
			this.radiusHelpMessage = I18n.format("ui.wondrousmagitek.area_stabilizer.radius_select", maxRadius.get());
		});

		container.radius.registerCallback(radius -> {
			this.rangeSelector.setCurrent(radius.get());
		});
	}

	@Override
	protected void init() {
		super.init();

		final int x = this.getClientX();
		final int y = this.getClientY();

		AreaStabilizerContainer container = this.container;

		this.addButton(this.rangeSelector = new RangeSelector(x, y + 27, 50, 16, 0, 100, 15, rangeSelector -> {
			container.radius.set(rangeSelector.getCurrent());
			ModNetworkChannel.sendToServer(new SetRangeCommandMessage(container));
			this.toggleOrUpdateVisualization(false);
		}));
		this.addButton(this.button = new Button(x, y + 50, 60,20, I18n.format("ui.wondrousmagitek.area_stabilizer.visualize_button"), button -> {
			this.toggleOrUpdateVisualization(true);
		}));
	}

	private void toggleOrUpdateVisualization(boolean toggle) {
		boolean visualizationActive = ScopedState.areaStabilizerVisualization != null;

		if(!toggle && !visualizationActive) {
			return;
		}

		if(toggle && visualizationActive) {
			ScopedState.areaStabilizerVisualization = null;
			return;
		}

		ScopedState.areaStabilizerVisualization = new Tuple<>(new ChunkPos(container.getTileEntity().getPos()), container.radius.get());
	}

	@Override
	protected void drawClientBackground(float partialTicks, int mouseX, int mouseY) { }

	@Override
	protected void drawClientForeground(int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GL11.glScalef(0.8f, 0.8f, 0.8f);
		this.font.drawString(this.radiusHelpMessage, 10f, 30f, 0xFF000000);
		GL11.glPopMatrix();
	}
}
