<%@page import="com.liferay.portal.kernel.util.Constants"%>
<%@ include file="init.jsp"%>
<portlet:resourceURL var="getResults">
	<portlet:param name="<%=Constants.CMD%>" value="get_results" />
</portlet:resourceURL>
<h2>Liferay Auto Complete List with Ajax</h2>
<br />
<aui:input id="myInputNode" name="myInputNode" label="Journal Article Search"
	helpMessage="Type the word you wanna search for" />
<aui:script>
	AUI()
			.use(
					'autocomplete-list',
					'aui-base',
					'aui-io-request',
					'autocomplete-filters','autocomplete-highlighters',function (A) {
						var testData;
						new A.AutoCompleteList({
									allowBrowserAutocomplete : 'true',
									activateFirstItem : 'true',
									inputNode : '#<portlet:namespace />myInputNode',
									resultTextLocator : 'searchFor',
									render : 'true',
									resultHighlighter : 'phraseMatch',
									resultFilters : [ 'phraseMatch' ],
									source : function() {
										var inputValue = A
												.one(
														"#<portlet:namespace />myInputNode")
												.get('value');
										var myAjaxRequest=A.io.request('<%=getResults.toString()%>',{
															dataType : 'json',
															method : 'POST',
															data : {
																<portlet:namespace />searchWord: inputValue,
															},
															autoLoad : false,
															sync : false,
															on : {
																success : function() {
																	var data = this .get('responseData');
																	testData = data;
																}
															}
														});
										myAjaxRequest.start();
										return testData;
									},
								});
					});

	
</aui:script>