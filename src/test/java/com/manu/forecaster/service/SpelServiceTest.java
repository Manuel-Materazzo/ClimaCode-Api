package com.manu.forecaster.service;

import com.manu.forecaster.dto.tile.TileRapresentation;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpelServiceTest {

	private final SpelService spelService = new SpelService();

	@Test
	void applyTemplates_nullSource_returnsEmpty() {
		String result = spelService.applyTemplates(null, Collections.emptyMap(), null);
		assertEquals("", result);
	}

	@Test
	void applyTemplates_blankSource_returnsEmpty() {
		String result = spelService.applyTemplates("   ", Collections.emptyMap(), null);
		assertEquals("", result);
	}

	@Test
	void applyTemplates_withTile_replacesTilePlaceholders() {
		TileRapresentation tile = TileRapresentation.builder().x(10).y(20).z(5).build();
		String result = spelService.applyTemplates("{x}-{y}-{z}", Collections.emptyMap(), tile);
		assertEquals("10-20-5", result);
	}

	@Test
	void applyTemplates_nullTile_doesNotReplaceTilePlaceholders() {
		String result = spelService.applyTemplates("{x}-{y}-{z}", Collections.emptyMap(), null);
		assertEquals("{x}-{y}-{z}", result);
	}

	@Test
	void applyTemplates_withSpelTemplate_replacesTemplateValue() {
		Map<String, String> templates = Map.of("result", "1+1");
		String result = spelService.applyTemplates("answer={result}", templates, null);
		assertEquals("answer=2", result);
	}

	@Test
	void applyTemplates_withTileAndSpelTemplates_replacesBoth() {
		TileRapresentation tile = TileRapresentation.builder().x(3).y(4).z(2).build();
		Map<String, String> templates = Map.of("result", "1+1");
		String result = spelService.applyTemplates("{x}-{y}-{z}-{result}", templates, tile);
		assertEquals("3-4-2-2", result);
	}

	@Test
	void applyTemplates_noMatchingPlaceholders_returnsUnchanged() {
		TileRapresentation tile = TileRapresentation.builder().x(1).y(2).z(3).build();
		String result = spelService.applyTemplates("no placeholders here", Collections.emptyMap(), tile);
		assertEquals("no placeholders here", result);
	}

	@Test
	void applyTemplates_emptyTemplatesMap_onlyReplacesTile() {
		TileRapresentation tile = TileRapresentation.builder().x(7).y(8).z(9).build();
		String result = spelService.applyTemplates("{x}/{y}/{z}", Collections.emptyMap(), tile);
		assertEquals("7/8/9", result);
	}

	@Test
	void parseSpel_simpleArithmetic_returnsResult() {
		String result = spelService.parseSpel("1+1");
		assertEquals("2", result);
	}

	@Test
	void parseSpel_stringConcatenation_returnsConcatenated() {
		String result = spelService.parseSpel("'hello' + ' world'");
		assertEquals("hello world", result);
	}

	@Test
	void parseSpel_staticMethodCall_returnsResult() {
		String result = spelService.parseSpel("T(java.lang.Math).max(1,2)");
		assertEquals("2", result);
	}

}
