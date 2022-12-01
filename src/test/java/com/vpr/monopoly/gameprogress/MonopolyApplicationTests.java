package com.vpr.monopoly.gameprogress;

import com.vpr.monopoly.gameprogress.model.PlayerDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
class MonopolyApplicationTests {

	@Test
	void contextLoads() {

		Map<String, Integer> map = Map.of(
				"White", 2,
				"Yellow", 3
		);

		String color = "White";
		System.out.println(map.containsKey(color));
	}

}
