package doser.server.actions.documentannotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.entitydisambiguation.dpo.DisambiguatedEntity;
import doser.entitydisambiguation.dpo.Time;
import doser.entitydisambiguation.table.logic.Type;
import doser.language.Languages;
import doser.tools.RDFGraphOperations;
import doser.webclassify.annotation.AnnotateCategories;
import doser.webclassify.annotation.AnnotateEntities;
//import doser.webclassify.annotation.AnnotateTime;
import doser.webclassify.dpo.Document;
import doser.webclassify.dpo.DocumentStatistic;
import doser.webclassify.dpo.Paragraph;
import doser.webclassify.dpo.SimpleMainTopicInput;
import doser.webclassify.dpo.SimpleMainTopicOutput;

@Controller
@RequestMapping("/webclassify")
public class DocumentClassification {

	private static AnnotateEntities entityAnnotation = new AnnotateEntities();

	private static AnnotateCategories categoryAnnotation = new AnnotateCategories();

	public DocumentClassification() {
		super();
	}

	@RequestMapping(value = "/entityStatistic", method = RequestMethod.POST)
	public @ResponseBody DocumentStatistic<DisambiguatedEntity, Integer> annotateEntities(
			@RequestBody final Document request) {
		DocumentStatistic<DisambiguatedEntity, Integer> statistic = new DocumentStatistic<DisambiguatedEntity, Integer>();
		List<Paragraph> paragraphs = request.getParagraphs();
		for (Paragraph para : paragraphs) {
			Set<Paragraph> set = new HashSet<Paragraph>();
			set.add(para);
			Map<DisambiguatedEntity, Integer> map = entityAnnotation
					.createEntityMap(set, request.getInternLanguage());
			List<Map.Entry<DisambiguatedEntity, Integer>> sortedList = entityAnnotation
					.createEntityDistributionParagraph(map);
			DisambiguatedEntity disentity = entityAnnotation
					.extractTopicEntity(map, para);
			List<Time> time = null;
//			AnnotateTime.getInstance().annotateTime(map,
//					para.getContent());
			statistic.addStatistic(para.getId(), para.getHeadline(),
					para.getContent(), disentity, time, sortedList);
		}
		return statistic;
	}

	@RequestMapping(value = "/entityAndCategoryStatistic", method = RequestMethod.POST)
	public @ResponseBody DocumentStatistic<DisambiguatedEntity, Integer> annotateEntitiesAndCategories(
			@RequestBody final Document request) {
		DocumentStatistic<DisambiguatedEntity, Integer> statistic = new DocumentStatistic<DisambiguatedEntity, Integer>();
		List<Paragraph> paragraphs = request.getParagraphs();
		for (Paragraph para : paragraphs) {
			Set<Paragraph> set = new HashSet<Paragraph>();
			set.add(para);
			Map<DisambiguatedEntity, Integer> map = entityAnnotation
					.createEntityMap(set, request.getInternLanguage());

			List<Map.Entry<DisambiguatedEntity, Integer>> sortedList = entityAnnotation
					.createEntityDistributionParagraph(map);
			DisambiguatedEntity disentity = entityAnnotation
					.extractTopicEntity(map, para);
			for (Map.Entry<DisambiguatedEntity, Integer> entry : sortedList) {
				categoryAnnotation.annotateCategories(entry.getKey(), request.getInternLanguage());
			}
			List<Time> time = null;
					//AnnotateTime.getInstance().annotateTime(map,
//					para.getContent());
			statistic.addStatistic(para.getId(), para.getHeadline(),
					para.getContent(), disentity, time, sortedList);
		}
		return statistic;
	}

	@RequestMapping(value = "/extractRelevantEntitiesPerParagraph", method = RequestMethod.POST)
	public @ResponseBody DocumentStatistic<DisambiguatedEntity, Integer> extractSignificantEntities(
			@RequestBody final Document request) {
		DocumentStatistic<DisambiguatedEntity, Integer> statistic = new DocumentStatistic<DisambiguatedEntity, Integer>();
		Set<Paragraph> set = new HashSet<Paragraph>(request.getParagraphs());
		for (Paragraph p : set) {
			List<DisambiguatedEntity> l = entityAnnotation
					.extractSignificantEntitiesInParagraph(p,
							request.getInternLanguage());
		}
		// statistic.setDocumentStatistic(l);
		return statistic;
	}

	@RequestMapping(value = "/extractMainTopic", method = RequestMethod.POST)
	public @ResponseBody SimpleMainTopicOutput extractMainTopic(
			@RequestBody final SimpleMainTopicInput request) {
		Set<Paragraph> set = new HashSet<Paragraph>();
		Paragraph para = new Paragraph();
		para.setHeadline("");
		para.setContent(request.getInput());
		para.setId("0");
		set.add(para);
		Map<DisambiguatedEntity, Integer> map = entityAnnotation
				.createEntityMap(set, Languages.english);
		DisambiguatedEntity disentity = entityAnnotation.extractTopicEntity(
				map, para);
		SimpleMainTopicOutput output = new SimpleMainTopicOutput();
		output.setUri(disentity.getEntityUri());
		output.setLabel(disentity.getText());
		Set<Type> cats = RDFGraphOperations.getRDFTypesFromEntity(disentity
				.getEntityUri());
		List<String> categories = new ArrayList<String>();
		for(Type t : cats) {
			categories.add(t.getUri());
		}
		output.setCategories(categories);
		return output;
	}
}
