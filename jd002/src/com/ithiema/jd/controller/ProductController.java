package com.ithiema.jd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ithiema.jd.pojo.QueryVo;
import com.ithiema.jd.pojo.ResultModel;
import com.ithiema.jd.service.ProductService;

@Controller
public class ProductController {

	@Autowired
	private ProductService productService;
	@RequestMapping(value="list.action")
	public String queryProducts(QueryVo vo,Model model){
		ResultModel resultModel = productService.queryProducts(vo);
		model.addAttribute("result",resultModel);
		model.addAttribute("queryString", vo.getQueryString());
		model.addAttribute("catalog_name", vo.getCatalog_name());
		model.addAttribute("page", vo.getPage());
		model.addAttribute("price", vo.getPrice());
		model.addAttribute("sort", vo.getSort());
		return "product_list";
	}
}
