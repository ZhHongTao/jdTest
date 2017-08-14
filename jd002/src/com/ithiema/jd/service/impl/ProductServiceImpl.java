package com.ithiema.jd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ithiema.jd.pojo.ProductModel;
import com.ithiema.jd.pojo.QueryVo;
import com.ithiema.jd.pojo.ResultModel;
import com.ithiema.jd.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private HttpSolrServer httpSolrServer;

	@Override
	public ResultModel queryProducts(QueryVo vo) {
		ResultModel result = new ResultModel();
		SolrQuery query = new SolrQuery();
		//拼装查询条件
		   //主要查询条件
		if(StringUtils.isNotBlank(vo.getQueryString())){
			query.setQuery(vo.getQueryString());
		}else{
			query.setQuery("*");
		}
		//商品价格
		//多个fq条件查询
		//SolrQuery queryZi = new SolrQuery();
		if(StringUtils.isNotBlank(vo.getPrice())){
			String price = vo.getPrice();
			String[] s = price.split("-");
			//queryZi.addFacetQuery("product_price:["+s[0]+" TO "+s[1]+"]");
			query.set("fq", "product_price:["+s[0]+" TO "+s[1]+"]");
		}
		//商品分类
		if(StringUtils.isNotBlank(vo.getCatalog_name())){
			//queryZi.addFacetQuery("product_catalog_name:"+vo.getCatalog_name());
			query.set("fq","product_catalog_name:"+vo.getCatalog_name());
		}
		//query.add(queryZi);
		//排序
		if(StringUtils.isNotBlank(vo.getSort())){
			String sort = vo.getSort();
			if("0".equals(vo.getSort())){
				query.addSort("product_price",ORDER.asc );
			}else{
				query.addSort("product_price",ORDER.desc );
			}
		}
		//分页
		int page=0;
		if(StringUtils.isBlank(vo.getPage())){
		    page=1;
		}else{
			page =Integer.valueOf(vo.getPage());
		}
		
		
		query.setStart((page-1)*8);
		query.setRows(8);
		//设置默认搜索区域
		query.set("df","product_name");
		//指定查询区域
		query.set("fl", "id,product_name,product_price,product_picture");
		//设置高亮
		   //开启高亮
		query.setHighlight(true);
		   //设置高亮的字段
		query.addHighlightField("product_name");
		   //设置高亮的前部
		query.setHighlightSimplePre("<span style='color:red'>");
		   //设置高亮的尾部
		query.setHighlightSimplePost("</span>");
		//result有四个属性
		// 当前页
		result.setCurPage(page);
		System.out.println(query);
		try {
			QueryResponse response = httpSolrServer.query(query);
			SolrDocumentList results = response.getResults();
			long numFound = results.getNumFound();
			// 商品总数
			result.setRecordCount(numFound);
			// 商品列表
			List<ProductModel> products = new ArrayList<>();
			for (SolrDocument doc : results) {
				ProductModel p = new ProductModel();
				//设置id
				String id = (String)doc.get("id");
				p.setPid(id);
				//设置价格
				Float price= (float)doc.get("product_price");
				p.setPrice(price);
				//设置图片
				String picture = (String) doc.get("product_picture");
				p.setPicture(picture);
				//设置高亮名字
				Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
				Map<String, List<String>> map = highlighting.get(id);
				if(map.size()>0){
					List<String> list = map.get("product_name");
					p.setName(list.get(0));
				}else{
					p.setName((String)doc.get("product_name"));
				}
				products.add(p);
			}
		result.setProductList(products);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return result;
	}
}
