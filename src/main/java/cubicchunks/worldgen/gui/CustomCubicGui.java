/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks.worldgen.gui;

import com.google.common.base.Converter;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.client.gui.event.component.SpaceChangeEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

import cubicchunks.util.MathUtil;
import cubicchunks.worldgen.generator.custom.CustomGeneratorSettings;
import cubicchunks.worldgen.gui.component.UIColoredPanel;
import cubicchunks.worldgen.gui.component.UIGridContainer;
import cubicchunks.worldgen.gui.component.UIMultilineLabel;
import cubicchunks.worldgen.gui.component.UITabbedContainer;
import cubicchunks.worldgen.gui.component.UIRangeSlider;
import cubicchunks.worldgen.gui.component.UISliderNoScroll;
import cubicchunks.worldgen.gui.converter.ConverterWithInfinity;
import cubicchunks.worldgen.gui.converter.Converters;
import cubicchunks.worldgen.gui.converter.ExponentialConverter;
import cubicchunks.worldgen.gui.converter.RoundingConverter;
import mcp.MethodsReturnNonnullByDefault;

import static cubicchunks.worldgen.gui.component.UIGridContainer.WIDTH_1_COL;
import static cubicchunks.worldgen.gui.component.UIGridContainer.WIDTH_2_COL;
import static cubicchunks.worldgen.gui.component.UIGridContainer.WIDTH_3_COL;
import static java.lang.Math.round;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomCubicGui extends ExtraGui {

	private static final int VERTICAL_PADDING = 30;
	private static final int HORIZONTAL_PADDING = 25;
	private static final int VERTICAL_INSETS = 2;
	private static final int HORIZONTAL_INSETS = 4;
	private static final int PREV_NEXT_WIDTH = 60;

	private final GuiCreateWorld parent;
	private UITabbedContainer tabs;

	public CustomCubicGui(GuiCreateWorld parent) {
		super();
		this.parent = parent;
	}

	/**
	 * Called before display() if this {@link MalisisGui} is not constructed yet.<br>
	 * Called when Ctrl+R is pressed to rebuild the GUI.
	 */
	@Override
	public void construct() {
		tabs = makeTabContainer();
		tabs.addTab(inPanel(createBasicSettingsTab()), vanillaText("basic_tab_title"));
		tabs.addTab(inPanel(createOreSettingsTab()), vanillaText("ores_tab_title"));
		tabs.addTab(inPanel(createAdvancedTerrainShapeTab()), vanillaText("advanced_tab_title"));
		addToScreen(tabs);
	}

	private UIContainer<?> inPanel(UIComponent<?> comp) {
		UIColoredPanel panel = new UIColoredPanel(this);
		panel.setSize(UIComponent.INHERITED, UIComponent.INHERITED - VERTICAL_PADDING*2);
		panel.setPosition(0, VERTICAL_PADDING);
		panel.add(comp);
		return panel;
	}

	private UITabbedContainer makeTabContainer() {

		final int buttonY = VERTICAL_PADDING/2 - 10;
		UIButton prev = new UIButton(this, malisisText("previous_page"))
			.setPosition(HORIZONTAL_PADDING + HORIZONTAL_INSETS, buttonY, Anchor.LEFT).setSize(PREV_NEXT_WIDTH, 20);
		UIButton next = new UIButton(this, malisisText("next_page"))
			.setPosition(-(HORIZONTAL_PADDING + HORIZONTAL_INSETS), buttonY, Anchor.RIGHT).setSize(PREV_NEXT_WIDTH, 20);

		UIMultilineLabel label = new UIMultilineLabel(this)
			.setPosition(0, 2)
			.setAnchor(Anchor.CENTER)
			.setTextAnchor(Anchor.CENTER)
			.setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build());

		UITabbedContainer tabGroup = new UITabbedContainer(this, prev, next, label::setText);
		tabGroup.add(label);

		return tabGroup;
	}

	private UIContainer<?> createBasicSettingsTab() {

		CustomGeneratorSettings settings = CustomGeneratorSettings.defaults();

		UIGridContainer layout = new UIGridContainer(this);
		layout.setPadding(HORIZONTAL_PADDING, 0);
		layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED)
			.setColumns(6)
			.setInsets(VERTICAL_INSETS, VERTICAL_INSETS, HORIZONTAL_INSETS, HORIZONTAL_INSETS)

			.add(makeCheckbox(malisisText("caves"), settings.caves),
				WIDTH_2_COL*0, 0, WIDTH_2_COL, 1)
			.add(makeCheckbox(malisisText("strongholds"), settings.strongholds),
				WIDTH_2_COL*1, 0, WIDTH_2_COL, 1)

			.add(makeCheckbox(malisisText("villages"), settings.villages),
				WIDTH_2_COL*0, 1, WIDTH_2_COL, 1)
			.add(makeCheckbox(malisisText("mineshafts"), settings.mineshafts),
				WIDTH_2_COL*1, 1, WIDTH_2_COL, 1)

			.add(makeCheckbox(malisisText("temples"), settings.temples),
				WIDTH_2_COL*0, 2, WIDTH_2_COL, 1)
			.add(makeCheckbox(malisisText("ravines"), settings.ravines),
				WIDTH_2_COL*1, 2, WIDTH_2_COL, 1)

			.add(makeCheckbox(malisisText("oceanMonuments"), settings.oceanMonuments),
				WIDTH_2_COL*0, 3, WIDTH_2_COL, 1)
			.add(makeCheckbox(malisisText("woodlandMansions"), settings.woodlandMansions),
				WIDTH_2_COL*1, 3, WIDTH_2_COL, 1)


			.add(makeCheckbox(malisisText("dungeons"), settings.dungeons),
				WIDTH_2_COL*0, 4, WIDTH_2_COL, 1)
			.add(makeCheckbox(malisisText("waterLakes"), settings.waterLakes),
				WIDTH_2_COL*1, 4, WIDTH_2_COL, 1)

			.add(makeCheckbox(malisisText("lavaLakes"), settings.lavaLakes),
				WIDTH_2_COL*0, 5, WIDTH_2_COL, 1)
			.add(makeCheckbox(malisisText("lavaOceans"), settings.lavaOceans),
				WIDTH_2_COL*1, 5, WIDTH_2_COL, 1)


			.add(makeBiomeList(), WIDTH_2_COL*0, 6, WIDTH_2_COL, 1)
			.add(makeIntSlider(malisisText("dungeonCount", ": %d"), 1, 100, settings.dungeonCount),
				WIDTH_2_COL*1, 6, WIDTH_2_COL, 1)

			.add(makeIntSlider(malisisText("waterLakeRarity", ": %d"), 1, 100, settings.waterLakeRarity),
				WIDTH_2_COL*0, 7, WIDTH_2_COL, 1)
			.add(makeIntSlider(malisisText("lavaLakeRarity", ": %d"), 1, 100, settings.lavaLakeRarity),
				WIDTH_2_COL*1, 7, WIDTH_2_COL, 1)

			.add(makeIntSlider(malisisText("biomeSize", ": %d"), 1, 8, settings.biomeSize),
				WIDTH_2_COL*0, 8, WIDTH_2_COL, 1)
			.add(makeIntSlider(malisisText("riverSize", ": %d"), 1, 5, settings.riverSize),
				WIDTH_2_COL*1, 8, WIDTH_2_COL, 1)


			.add(makeExponentialSlider(
				malisisText("water_level", ": %.2f"),
				1, 12, 1, 12, settings.waterLevel),
				WIDTH_2_COL*0, 9, WIDTH_2_COL, 1)


			.init();

		return layout;
	}

	private UIContainer<?> createOreSettingsTab() {
		CustomGeneratorSettings settings = CustomGeneratorSettings.defaults();

		int y = -1;
		UIGridContainer layout = new UIGridContainer(this);
		layout.setPadding(HORIZONTAL_PADDING, 0);
		layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED)
			.setColumns(6)
			.setInsets(VERTICAL_INSETS, VERTICAL_INSETS, HORIZONTAL_INSETS, HORIZONTAL_INSETS)

			.add(label(malisisText("dirt_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.dirtSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.dirtSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.dirtSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.dirtSpawnMinHeight, settings.dirtSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("gravel_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.gravelSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.gravelSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.gravelSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.gravelSpawnMinHeight, settings.gravelSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("granite_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.graniteSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.graniteSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.graniteSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.graniteSpawnMinHeight, settings.graniteSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("diorite_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.dioriteSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.dioriteSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.dioriteSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.dioriteSpawnMinHeight, settings.dioriteSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("andesite_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.andesiteSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.andesiteSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.andesiteSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.andesiteSpawnMinHeight, settings.andesiteSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("coal_ore_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.coalOreSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.coalOreSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.coalOreSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.coalOreSpawnMinHeight, settings.coalOreSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("iron_ore_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.ironOreSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.ironOreSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.ironOreSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.ironOreSpawnMinHeight, settings.ironOreSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("gold_ore_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.goldOreSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.goldOreSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.goldOreSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.goldOreSpawnMinHeight, settings.goldOreSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("redstone_ore_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.redstoneOreSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.redstoneOreSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.redstoneOreSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.redstoneOreSpawnMinHeight, settings.redstoneOreSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("diamond_ore_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.diamondOreSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.diamondOreSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.diamondOreSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.diamondOreSpawnMinHeight, settings.diamondOreSpawnMaxHeight),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.add(label(malisisText("lapis_lazuli_ore_group"), 20),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)
			.add(makeIntSlider(malisisText("spawn_size", " %d"), 1, 50, settings.lapisLazuliSpawnSize),
				WIDTH_3_COL*0, ++y, WIDTH_3_COL, 1)
			.add(makeIntSlider(malisisText("spawn_tries", " %d"), 1, 40, settings.lapisLazuliSpawnTries),
				WIDTH_3_COL*1, y, WIDTH_3_COL, 1)
			.add(makeFloatSlider(malisisText("spawn_probability", " %.3f"), settings.lapisLazuliSpawnProbability),
				WIDTH_3_COL*2, y, WIDTH_3_COL, 1)
			.add(makeRangeSlider(vanillaText("spawn_range"), -2.0f, 2.0f, settings.lapisLazuliSpawnCenter - settings.lapisLazuliSpawnSpread, settings.lapisLazuliSpawnCenter + settings.lapisLazuliSpawnSpread),
				WIDTH_1_COL*0, ++y, WIDTH_1_COL, 1)

			.init();

		return layout;
	}

	private UIContainer<?> createAdvancedTerrainShapeTab() {

		final float MAX_NOISE_FREQ_POWER = -4;

		CustomGeneratorSettings settings = CustomGeneratorSettings.defaults();

		String PERIOD_FMT = " %.2f";
		UIGridContainer layout = new UIGridContainer(this);
		layout.setPadding(HORIZONTAL_PADDING, 0);
		layout.setSize(UIComponent.INHERITED, UIComponent.INHERITED)
			.setColumns(6)
			.setInsets(VERTICAL_INSETS, VERTICAL_INSETS, HORIZONTAL_INSETS, HORIZONTAL_INSETS)
			// height variation
			.add(label(malisisText("height_variation_group"), 20),
				WIDTH_1_COL*0, 0, WIDTH_1_COL, 1)
			.add(makeExponentialSlider(
				malisisText("height_variation_factor_slider", ": %.2f"),
				Float.NaN, Float.NaN, 0, 10, settings.heightVariationFactor),
				WIDTH_3_COL*0, 1, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("height_variation_special_factor_slider", ": %.2f"),
				Float.NaN, Float.NaN, 0, 10, settings.specialHeightVariationFactorBelowAverageY),
				WIDTH_3_COL*1, 1, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("height_variation_offset_slider", ": %.2f"),
				0, 10, 0, 10, settings.heightVariationOffset),
				WIDTH_3_COL*2, 1, WIDTH_3_COL, 1)

			// height
			.add(label(malisisText("height_group"), 20),
				WIDTH_1_COL*0, 2, WIDTH_1_COL, 1)
			.add(makeExponentialSlider(
				malisisText("height_factor", ": %.2f"),
				1, 12, 1, 12, settings.heightFactor),
				WIDTH_2_COL*0, 3, WIDTH_2_COL, 1)
			.add(makeExponentialSlider(
				malisisText("height_offset", ": %.2f"),
				1, 12, 1, 12, settings.heightOffset),
				WIDTH_2_COL*1, 3, WIDTH_2_COL, 1)

			// depth noise
			.add(label(malisisText("depth_noise_group"), 20),
				WIDTH_1_COL*0, 6, WIDTH_1_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("depth_noise_period_x", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.depthNoiseFrequencyX),
				WIDTH_2_COL*0, 7, WIDTH_2_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("depth_noise_period_z", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.depthNoiseFrequencyZ),
				WIDTH_2_COL*1, 7, WIDTH_2_COL, 1)

			.add(makeIntSlider(
				malisisText("depth_noise_octaves", ": %d"),
				1, 16, settings.depthNoiseOctaves),
				WIDTH_3_COL*0, 8, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("depth_noise_factor", ": %.4f"),
				Float.NaN, Float.NaN, 1, 12, settings.depthNoiseFactor),
				WIDTH_3_COL*1, 8, WIDTH_3_COL, 1)
			.add(
				makeExponentialSlider(
					malisisText("depth_noise_offset", ": %.2f"),
					1, 12, 1, 12, settings.depthNoiseOffset),
				WIDTH_3_COL*2, 8, WIDTH_3_COL, 1)

			// selector noise
			.add(label(malisisText("selector_noise_group"), 20),
				WIDTH_1_COL*0, 9, WIDTH_1_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("selector_noise_period_x", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.selectorNoiseFrequencyX),
				WIDTH_3_COL*0, 10, WIDTH_3_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("selector_noise_period_y", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.selectorNoiseFrequencyY),
				WIDTH_3_COL*1, 10, WIDTH_3_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("selector_noise_period_z", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.selectorNoiseFrequencyZ),
				WIDTH_3_COL*2, 10, WIDTH_3_COL, 1)

			.add(makeIntSlider(
				malisisText("selector_noise_octaves", ": %d"),
				1, 16, settings.selectorNoiseOctaves),
				WIDTH_3_COL*0, 11, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("selector_noise_factor", ": %.4f"),
				Float.NaN, Float.NaN, 0, 10, settings.selectorNoiseFactor),
				WIDTH_3_COL*1, 11, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("selector_noise_offset", ": %.2f"),
				-5, 5, -5, 5, settings.selectorNoiseOffset),
				WIDTH_3_COL*2, 11, WIDTH_3_COL, 1)


			// low noise
			.add(label(malisisText("low_noise_group"), 20),
				WIDTH_1_COL*0, 12, WIDTH_1_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("low_noise_period_x", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.lowNoiseFrequencyX),
				WIDTH_3_COL*0, 13, WIDTH_3_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("low_noise_period_y", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.lowNoiseFrequencyY),
				WIDTH_3_COL*1, 13, WIDTH_3_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("low_noise_period_z", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.lowNoiseFrequencyZ),
				WIDTH_3_COL*2, 13, WIDTH_3_COL, 1)

			.add(makeIntSlider(
				malisisText("low_noise_octaves", ": %d"),
				1, 16, settings.lowNoiseOctaves),
				WIDTH_3_COL*0, 14, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("low_noise_factor", ": %.4f"),
				Float.NaN, Float.NaN, 0, 10, settings.lowNoiseFactor),
				WIDTH_3_COL*1, 14, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("low_noise_offset", ": %.2f"),
				-5, 5, -5, 5, settings.lowNoiseOffset),
				WIDTH_3_COL*2, 14, WIDTH_3_COL, 1)

			// high noise
			.add(label(malisisText("high_noise_group"), 20),
				WIDTH_1_COL*0, 15, WIDTH_1_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("high_noise_period_x", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.highNoiseFrequencyX),
				WIDTH_3_COL*0, 16, WIDTH_3_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("high_noise_period_y", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.highNoiseFrequencyY),
				WIDTH_3_COL*1, 16, WIDTH_3_COL, 1)
			.add(makeInvertedExponentialSlider(
				malisisText("high_noise_period_z", PERIOD_FMT),
				Float.NaN, Float.NaN, -8, MAX_NOISE_FREQ_POWER, 1.0f/settings.highNoiseFrequencyZ),
				WIDTH_3_COL*2, 16, WIDTH_3_COL, 1)

			.add(makeIntSlider(
				malisisText("high_noise_octaves", ": %d"),
				1, 16, settings.highNoiseOctaves),
				WIDTH_3_COL*0, 17, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("high_noise_factor", ": %.4f"),
				Float.NaN, Float.NaN, 0, 10, settings.highNoiseFactor),
				WIDTH_3_COL*1, 17, WIDTH_3_COL, 1)
			.add(makeExponentialSlider(
				malisisText("high_noise_offset", ": %.2f"),
				-5, 5, -5, 5, settings.highNoiseOffset),
				WIDTH_3_COL*2, 17, WIDTH_3_COL, 1)

			.init();
		return layout;
	}

	private UIComponent<?> makeExponentialSlider(String name, float minNeg, float maxNeg, float minPos, float maxPos, float defaultVal) {

		UISlider<Float>[] wrappedSlider = new UISlider[1];
		DoubleUnaryOperator roundRadiusFunc = d -> 1.0/(wrappedSlider[0] == null ? 1000 : wrappedSlider[0].getWidth())*0.5;

		float defMult = defaultVal == 0 ? 1 : defaultVal;

		Converter<Float, Float> conv = Converters.builder()
			.exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos)
			.rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128).withRoundingRadius(roundRadiusFunc)
			.build();

		UISlider<Float> slider = new UISliderNoScroll<>(this, this.width - 32, conv, name).setValue(defaultVal);
		wrappedSlider[0] = slider;
		return slider;
	}

	private UIComponent<?> makeInvertedExponentialSlider(String name, float minNeg, float maxNeg, float minPos, float maxPos, float defaultVal) {

		UISlider<Float>[] wrappedSlider = new UISlider[1];
		DoubleUnaryOperator roundRadiusFunc = d -> 1.0/(wrappedSlider[0] == null ? 1000 : wrappedSlider[0].getWidth())*0.5;

		float defMult = defaultVal == 0 ? 1 : defaultVal;

		Converter<Float, Float> conv = Converters.builder()
			.reverse()
			.pow(2)
			.exponential().withZero().withBaseValue(2).withNegativeExponentRange(minNeg, maxNeg).withPositiveExponentRange(minPos, maxPos)
			.inverse()
			.rounding().withBase(2, 1).withBase(10, 1).withBase(2, defMult).withBase(10, defMult).withMaxExp(128).withRoundingRadius(roundRadiusFunc)
			.build();

		UISlider<Float> slider = new UISliderNoScroll<>(this, this.width - 32, conv, name).setValue(defaultVal);
		wrappedSlider[0] = slider;
		return slider;
	}

	private UIComponent<?> makeIntSlider(String name, int min, int max, int defaultValue) {
		// the explicit <Integer> needs to be there because otherwise it won't compile on some systems
		UISlider<Integer> slider = new UISliderNoScroll<Integer>(
			this,
			this.width,
			Converter.from(
				x -> round(x*(max - min) + min),
				x -> (x - min)/((float) max - min))
			, name)
			.setValue(defaultValue)
			.setSize(0, 20);
		return slider;
	}

	private UIComponent<?> makeFloatSlider(String name, float defaultValue) {
		// the explicit <Integer> needs to be there because otherwise it won't compile on some systems
		UISlider<Float> slider = new UISliderNoScroll<Float>(
			this,
			this.width,
			Converter.identity(),
			name)
			.setValue(defaultValue)
			.setSize(0, 20);
		return slider;
	}

	public UIComponent<?> makeCheckbox(String name, boolean defaultValue) {
		UICheckBox cb = new UICheckBox(this, name)
			.setChecked(defaultValue)
			.setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build());
		return cb;
	}

	private UIComponent<?> makeRangeSlider(String name, float min, float max, float defaultMin, float defaultMax) {
		UIRangeSlider<Float>[] wrappedSlider = new UIRangeSlider[1];
		DoubleUnaryOperator roundRadiusFunc = d -> 1.0/(wrappedSlider[0] == null ? 1000 : wrappedSlider[0].getWidth())*0.5;
		float maxExp = MathHelper.ceil(Math.log(Math.max(1, max))/Math.log(2));

		Converter<Float, Float> conv = Converters.builder()
			.linearScale(min, max)
			.rounding().withBase(2, 1).withBase(10, 1).withMaxExp(maxExp).withRoundingRadius(roundRadiusFunc)
			.withInfinity().negativeAt(min).positiveAt(max)
			.build();

		UIRangeSlider<Float> slider = new UIRangeSlider<Float>(
			this, 100,
			conv,
			(a, b) -> I18n.format(name, a*100, b*100))
			.setRange(defaultMin, defaultMax);
		wrappedSlider[0] = slider;
		return slider;
	}

	private UIComponent<?> makeBiomeList() {
		List<BiomeOption> biomes = new ArrayList<>();
		biomes.add(BiomeOption.ALL);
		for (Biome biome : ForgeRegistries.BIOMES) {
			if (!biome.isMutation()) {
				biomes.add(new BiomeOption(biome));
			}
		}
		UISelect<BiomeOption> select = new UISelect<>(this, 0, biomes);
		select.register(new Object() {
			@Subscribe
			public void onResize(SpaceChangeEvent.SizeChangeEvent evt) {
				select.setMaxExpandedWidth(evt.getNewWidth());
			}
		});
		select.select(BiomeOption.ALL);
		select.maxDisplayedOptions(8);
		return select;
	}

	private UIComponent<?> label(String text, int height) {
		return wrappedCentered(
			new UILabel(this, text)
				.setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build())
		).setSize(0, height);
	}

	private UIContainer<?> wrappedCentered(UIComponent<?> comp) {
		comp.setAnchor(Anchor.MIDDLE | Anchor.CENTER);
		UIContainer<?> cont = new UIContainer<>(this);
		cont.add(comp);
		return cont;
	}

	private static String vanillaText(String name) {
		String unloc = "cubicchunks.gui.customcubic." + name;
		return unloc;
	}

	private static String malisisText(String name) {
		String unloc = "{cubicchunks.gui.customcubic." + name + "}";
		return unloc;
	}

	private static String malisisText(String name, String fmt) {
		String unloc = "{cubicchunks.gui.customcubic." + name + "}" + fmt;
		return unloc;
	}
}
