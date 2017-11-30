package com.liferay.autocomplete.portlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
//import com.liferay.journal.search.JournalArticleIndexer;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;

/**
 * @author carlos
 */
@Component(immediate = true, property = { "com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=true", "javax.portlet.display-name=autocomplete Portlet",
		"javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=Autocomplete", "javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user" }, service = Portlet.class)
public class AutocompletePortlet extends MVCPortlet {
	// @Reference
	// JournalArticleIndexer jaIndexer;

	private static final String JOURNAL_ARTICLE = "com.liferay.journal.model.JournalArticle";
	long companyId = PortalUtil.getDefaultCompanyId();
	int maxNumOfQueryElems = 10000;

	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws IOException, PortletException {

		LiferayPortletRequest portletRequest = PortalUtil.getLiferayPortletRequest(resourceRequest);
		LiferayPortletResponse portletResponse = PortalUtil.getLiferayPortletResponse(resourceResponse);

		HttpServletRequest request = PortalUtil.getHttpServletRequest(resourceRequest);
		String searchword = ParamUtil.getString(resourceRequest, "searchWord");

		SearchContext searchContext = SearchContextFactory.getInstance(request);
		JSONObject json = executeSearchQuery(searchword, searchContext,portletRequest,portletResponse);

		PrintWriter out = resourceResponse.getWriter();
		out.println(json.toString());
	}

	/**
	 * {"query": { "bool": { "must": [ { "match": { "title": "hello" } } ] } }}
	 * @param portletResponse 
	 * @param portletRequest 
	 * 
	 * @return
	 */
	private JSONObject executeSearchQuery(String searchword, SearchContext searchContext, LiferayPortletRequest portletRequest, LiferayPortletResponse portletResponse) {

		JSONObject json = JSONFactoryUtil.createJSONObject();
		Indexer<JournalArticle> indexer = IndexerRegistryUtil.getIndexer(JournalArticle.class);
		AssetRendererFactory<JournalArticle> journalArticleAssetRendererFactory = AssetRendererFactoryRegistryUtil
				.getAssetRendererFactoryByClass(JournalArticle.class);
		try {
			searchContext.setKeywords(searchword);
			Hits hits = indexer.search(searchContext);
			System.out.println(hits.getLength());
			//I DON'T THINK IT IS NECESSARY TO DO THIS FILTER IN JAVA (elastic does it anyway for you; you're using the JournalArticle indexer)
			hits.toList().stream().filter(r -> r.get("entryClassName").equals(JOURNAL_ARTICLE)).forEach(r -> {
				long resourcePrimaryKey = Long.parseLong(r.get("entryClassPK"));
				JournalArticle ja;
				try {
					ja = JournalArticleLocalServiceUtil.getLatestArticle(resourcePrimaryKey);
					AssetRenderer<JournalArticle> ar = journalArticleAssetRendererFactory
						.getAssetRenderer(ja.getTrashEntryClassPK(), 1);
					String url = ar.getURLViewInContext(portletRequest, portletResponse, null);
					json.put(r.get(Field.TITLE),url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

}