package com.baechu.testfront;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Buydto {

	private int inventory;


	public Buydto(int inventory) {
		this.inventory = inventory;
	}



}
