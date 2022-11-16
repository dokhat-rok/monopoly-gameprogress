package com.vpr.monopoly.gameprogress;

import com.vpr.monopoly.gameprogress.model.PlayerDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
class MonopolyApplicationTests {

	@Test
	void contextLoads() {

		List<PlayerDto> playerDtoList = new java.util.ArrayList<>(List.of(
				PlayerDto.builder().money(1500L).build(),
				PlayerDto.builder().money(500L).build(),
				PlayerDto.builder().money(9500L).build(),
				PlayerDto.builder().money(100L).build(),
				PlayerDto.builder().money(700L).build()
		));

		playerDtoList.sort(Comparator.comparing(PlayerDto::getMoney));
		System.out.println(playerDtoList);
		PlayerDto playerDto = playerDtoList.stream().filter(playerDto1 -> playerDto1.getMoney().equals(500L)).findFirst().orElse(null);
		System.out.println(playerDto);
	}

}
