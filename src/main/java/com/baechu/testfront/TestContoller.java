package com.baechu.testfront;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestContoller {

	@GetMapping("/detail/{id}")
	public ModelAndView detailPage(@PathVariable Long id){

		ModelAndView modelAndView = new ModelAndView("detail");

		Map<String, Object> info = new HashMap<>();
		String title = id+"잡아의 정석";
		info.put("title", title);
		info.put("price", 190);
		info.put("image","https://image.aladin.co.kr/product/22460/28/cover200/8994492046_1.jpg");
		info.put("author", "나작가");
		info.put("publish", "항해999출판");
		info.put("birth", "2016년 8월");
		info.put("inventory", 7);

		Buydto buydto = new Buydto();

		modelAndView.addObject("info", info);
		modelAndView.addObject("butdto", buydto);

		return modelAndView;
	}



	//n권 주문까지 들어가지만 String 으로 리턴하여 에러가 난다
	@PostMapping("/detail/buybooks")
	public String Buybook(HttpServletRequest request){

		String ans = request.getParameter("inventory")+" 권 주문한다용";
		return ans;
	}



}
