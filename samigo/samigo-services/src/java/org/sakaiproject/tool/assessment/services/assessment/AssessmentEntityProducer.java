/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.services.assessment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.util.ImportPerformance;
import org.sakaiproject.util.api.LinkMigrationHelper;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssessmentEntityProducer implements EntityTransferrer, EntityProducer {

    private static final int QTI_VERSION = 1;
    private static final String ARCHIVED_ELEMENT = "assessment";
    private QTIServiceAPI qtiService;

	private final LinkMigrationHelper linkMigrationHelper = ComponentManager.get(LinkMigrationHelper.class);

	private Map<String, ImportPerformance> importPerformances = new HashMap<>();

	public void init() {
		log.info("init()");
		try {
			EntityManager.registerEntityProducer(this, Entity.SEPARATOR
					+ "samigo");
		} catch (Exception e) {
			log.warn("Error registering Samigo Entity Producer", e);
		}
	}

	public void destroy() {
	}

    public void setQtiService(QTIServiceAPI qtiService)  {
        this.qtiService = qtiService;
    }

	public String[] myToolIds() {
		String[] toolIds = { "sakai.samigo" };
		return toolIds;
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> resourceIds, List<String> transferOptions) {

		AssessmentService service = new AssessmentService();
		Map<String, String> transversalMap = new HashMap<String, String>();
		ImportPerformance importPerformance = importPerformances.computeIfAbsent(toContext, (key) -> new ImportPerformance());
		importPerformance.reset();
		log.info("Start messuring import performance to site {}, {} site(s) registeded.", toContext, importPerformances.size());
		importPerformance.startMeassuring();
		importPerformance.startMeassuring(ImportPerformance.COPY_ASSESSMENTS);
		service.copyAllAssessments(fromContext, toContext, transversalMap);
		importPerformance.stopMeassuring(ImportPerformance.COPY_ASSESSMENTS);
		// At a minimum, we need to remap all the attachment URLs to point to the new site

		transversalMap.put("/content/attachment/" + fromContext + "/", "/content/attachment/" + toContext + "/");
		return transversalMap;
	}

	public String archive(String siteId, Document doc, Stack stack,

                          String archivePath, List attachments) {
        StringBuilder results = new StringBuilder();
        results.append("archiving ").append(getLabel()).append("\n");

        String qtiPath = archivePath + File.separator + "qti";
        File qtiDir = new File(qtiPath);
        if (!qtiDir.isDirectory() && !qtiDir.mkdir()) {
            log.error("Could not create directory " + qtiPath);
            results.append("Could not create " + qtiPath + "\n");
            return  results.toString();
        }

        Element element = doc.createElement(this.getClass().getName());
        ((Element) stack.peek()).appendChild(element);
        stack.push(element);
        AssessmentService assessmentService = new AssessmentService();
        List<AssessmentData> assessmentList 
                = (List<AssessmentData>) assessmentService.getAllActiveAssessmentsbyAgent(siteId);
        for (AssessmentData data : assessmentList) {
            Element assessmentXml = doc.createElement(ARCHIVED_ELEMENT);
            String id = data.getAssessmentId().toString();
            assessmentXml.setAttribute("id", id);
            FileWriter writer = null;
            try {
                File assessmentFile = new File(qtiPath + File.separator + ARCHIVED_ELEMENT + id + ".xml");
                writer = new FileWriter(assessmentFile);
                writer.write(qtiService.getExportedAssessmentAsString(id, QTI_VERSION));
            } catch (IOException e) {
                results.append(e.getMessage() + "\n");
                log.error(e.getMessage(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                    }
                } 
            }
            element.appendChild(assessmentXml);

        }
        stack.pop();
		return results.toString();
	}

	public Entity getEntity(Reference ref) {
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	public String getEntityDescription(Reference ref) {
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	public String getEntityUrl(Reference ref) {
		return null;
	}

	public HttpAccess getHttpAccess() {
		return null;
	}

	public String getLabel() {
		return "samigo";
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
	if (log.isDebugEnabled()) log.debug("merging " + getLabel());
        StringBuilder results = new StringBuilder();
        String qtiPath = (new File(archivePath)).getParent() 
                         + File.separator + "qti" + File.separator;
        //TODO: replaced by getChildren when we make sure we have the
        NodeList assessments = root.getElementsByTagName(ARCHIVED_ELEMENT);

        DocumentBuilder dbuilder = null;
        try {
            dbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return getLabel() + " Error: " + t.getMessage();
        }

        for (int i=0; i<assessments.getLength(); ++i) {
            Element element = (Element) assessments.item(i);
            String id = element.getAttribute("id");
            String path = qtiPath + ARCHIVED_ELEMENT + id + ".xml";
            try {
                AssessmentIfc assessment = qtiService.createImportedAssessment(path, QTI_VERSION,  siteId);
                results.append(getLabel() + " imported assessment '" + assessment.getTitle() + "'\n");            
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
                results.append(getLabel() + " error with assessment "  
                               + id + ": " + t.getMessage() + "\n");
            }
        }
        return results.toString();
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	public boolean willArchiveMerge() {
		return true;
	}

	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

		try {
			if (cleanup) {
				if (log.isDebugEnabled()) log.debug("deleting assessments from " + toContext);
				AssessmentService service = new AssessmentService();
				ImportPerformance importPerformance = importPerformances.get(toContext);
				importPerformance.startMeassuring(ImportPerformance.GET_ASSESSMENTS);
				List assessmentList = service.getAllActiveAssessmentsbyAgent(toContext);
				importPerformance.stopMeassuring(ImportPerformance.GET_ASSESSMENTS);
				log.debug("found " + assessmentList.size() + " assessments in site: " + toContext);
				for (Iterator iter = assessmentList.iterator(); iter.hasNext();) {
					AssessmentData oneassessment = (AssessmentData) iter.next();
					log.debug("removing assessemnt id = " +oneassessment.getAssessmentId() );
					service.removeAssessment(oneassessment.getAssessmentId().toString());
				}
			}
		} catch (Exception e) {
			log.error("transferCopyEntities: End removing Assessment data", e);
		}
		
		return transferCopyEntities(fromContext, toContext, ids, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap){
		ImportPerformance importPerformance = importPerformances.get(toContext);
		importPerformance.startMeassuring(ImportPerformance.UPDATE_REFS);
		if(transversalMap != null && transversalMap.size() > 0){

			Set<Entry<String, String>> entrySet = (Set<Entry<String, String>>) transversalMap.entrySet();

			AssessmentService service = new AssessmentService();
		
			importPerformance.startMeassuring(ImportPerformance.GET_ASSESSMENTS_FOR_UPDATE);
			List<AssessmentData> assessmentList = service.getAllActiveAssessmentsbyAgent(toContext);			
			importPerformance.stopMeassuring(ImportPerformance.GET_ASSESSMENTS_FOR_UPDATE);

			
			importPerformance.startMeassuring(ImportPerformance.PREPARE_FOR_CACHING);
			Set<Long> assessmentIds = assessmentList.stream()
					.map(AssessmentData::getAssessmentBaseId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			Set<String> duplicateHashes = service.getDuplicateItemHashesForAssessmentIds(assessmentIds);
			importPerformance.stopMeassuring(ImportPerformance.PREPARE_FOR_CACHING);

			Map<String, Boolean> needToUpdateCache = new HashMap<>();
			Map<String, String> itemContentCache = new HashMap<>();

			Iterator assessmentIter =assessmentList.iterator();
			while (assessmentIter.hasNext()) {
				AssessmentData assessment = (AssessmentData) assessmentIter.next();		
				//get initialized assessment
				AssessmentFacade assessmentFacade = (AssessmentFacade) service.getAssessment(assessment.getAssessmentId());		
				boolean needToUpdate = false;
				
				String assessmentDesc = assessmentFacade.getDescription();
				if(StringUtils.isNotBlank(assessmentDesc)){
					importPerformance.startMeassuring(ImportPerformance.MIGRATE_LINKS);
					assessmentDesc = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, assessmentDesc);
					importPerformance.stopMeassuring(ImportPerformance.MIGRATE_LINKS);
					if(!assessmentDesc.equals(assessmentFacade.getDescription())){
						//need to save since a ref has been updated:
						needToUpdate = true;
						assessmentFacade.setDescription(assessmentDesc);
					}
				}
				
				List sectionList = assessmentFacade.getSectionArray();
				for(int i = 0; i < sectionList.size(); i++){
					SectionFacade section = (SectionFacade) sectionList.get(i);
					String sectionDesc = section.getDescription();
					if(StringUtils.isNotBlank(sectionDesc)){
						importPerformance.startMeassuring(ImportPerformance.MIGRATE_LINKS);
						sectionDesc = org.sakaiproject.util.cover.LinkMigrationHelper.migrateAllLinks(entrySet, sectionDesc);
						importPerformance.stopMeassuring(ImportPerformance.MIGRATE_LINKS);
						if(!sectionDesc.equals(section.getDescription())){
							//need to save since a ref has been updated:
							needToUpdate = true;
							section.setDescription(sectionDesc);
						}
					}

					List<ItemData> itemList = section.getItemArray();


					for (ItemData item : itemList) {
						importPerformance.startMeassuringItem(item);						

						String itemHash = item.getHash();
						boolean hasDuplicates = StringUtils.isNotEmpty(itemHash) && duplicateHashes.contains(itemHash);
						boolean hasCaches = hasDuplicates && needToUpdateCache.containsKey(itemHash);

						// If no update is required so far and we cached that an item does not need an update, we can skip the item
						if (hasCaches && !needToUpdateCache.get(itemHash)) {
							importPerformance.stopMeassuringItem(item);
							importPerformance.migrateSkipped();
							continue;
						}

						boolean instructionChanged = migrateText(service, toContext, item, itemHash, hasCaches, hasDuplicates, false,
								"inst", itemContentCache, entrySet, ItemData::getInstruction, ItemData::setInstruction);

						boolean descriptionChanged = migrateText(service, toContext, item, itemHash, hasCaches, hasDuplicates, false,
								"desc", itemContentCache, entrySet, ItemData::getDescription, ItemData::setDescription);

						boolean itemTextsChanged = false;
						List<ItemTextIfc> itemTexts = item.getItemTextArray();
						if (itemTexts != null) {
							for (int j = 0; j < itemTexts.size(); j++) {
								ItemTextIfc itemText = itemTexts.get(j);
								boolean itemTextChanged = migrateText(service, toContext, itemText, itemHash, hasCaches, hasDuplicates, true,
										"it-" + j, itemContentCache, entrySet, ItemTextIfc::getText, ItemTextIfc::setText);

								boolean answersChanged = false;
								List<AnswerIfc> answers =  itemText.getAnswerArray();
								if (answers != null) {
									for (int k = 0; k < answers.size(); k++) {
										AnswerIfc answer = answers.get(k);
										boolean answerChanged = migrateText(service, toContext, answer, itemHash, hasCaches, hasDuplicates, true,
												"at-" + j + "-"+ k , itemContentCache, entrySet, AnswerIfc::getText, AnswerIfc::setText);

										answersChanged = answersChanged || answerChanged;
									}
								}

								itemTextsChanged = itemTextsChanged || itemTextChanged || answersChanged;							
							}
						}

						needToUpdate = needToUpdate
								|| instructionChanged
								|| descriptionChanged
								|| itemTextsChanged;

						needToUpdateCache.put(itemHash, needToUpdate);
						
						importPerformance.stopMeassuringItem(item);
					}					
				}
				
				if(needToUpdate){
					//since the text changes were direct manipulations (no iterators),
					//hibernate will take care of saving everything that changed:
					service.saveAssessment(assessmentFacade);
				}
				// Update evaluation file after every assessment is processed
				savePerformanceEvaluation(toContext);
			}
		}
		importPerformance.stopMeassuring(ImportPerformance.UPDATE_REFS);
		importPerformance.stopMeassuring();
		log.info("Stop messuring import performance to site {}, {} site(s) registeded.", toContext, importPerformances.size());
		savePerformanceEvaluation(toContext);
	}
	
	private <T> boolean migrateText(AssessmentService assessmentService, String toContext, T item, String itemHash,
			boolean hasCaches,boolean hasDuplicates, boolean copyAttachments, String cacheCode, Map<String, String> textCache,
			Set<Entry<String, String>> entrySet, Function<T, String> getter, BiConsumer<T, String> setter) {

		ImportPerformance importPerformance = importPerformances.get(toContext);
		String cacheKey = itemHash + "-" + cacheCode;

		if (hasCaches && textCache.containsKey(cacheKey)) {
			// Item instruction has been cashed, lets get it form the cache
			setter.accept(item, textCache.get(cacheKey));
			return true;
		} else {
			// Item instruction has not been cached, lets try migrating
			String itemText = StringUtils.trimToEmpty(getter.apply(item));
			String migratedText;
			if (copyAttachments) {
				importPerformance.startMeassuring(ImportPerformance.COPY_ATTACHMANETS);
				migratedText = assessmentService.copyContentHostingAttachments(itemText, toContext);
				importPerformance.stopMeassuring(ImportPerformance.COPY_ATTACHMANETS);
			} else {
				migratedText = itemText;
			}

			importPerformance.startMeassuring(ImportPerformance.MIGRATE_LINKS);
			migratedText = linkMigrationHelper.migrateAllLinks(entrySet, migratedText);
			importPerformance.stopMeassuring(ImportPerformance.MIGRATE_LINKS);

			// Check if there has been a change
			if (!StringUtils.equals(itemText, migratedText)) {
				setter.accept(item, migratedText);

				if (hasDuplicates) {
					textCache.put(cacheKey, migratedText);
				}

				return true;
			}
		}
		
		return false;
	}

	private void savePerformanceEvaluation(String toContext) {
		String evaluationFolderName = StringUtils.replace(toContext, "/", "-");
		ImportPerformance importPerformance = importPerformances.get(toContext);

		Path evaluationFolderPath = Paths.get(ServerConfigurationService.getSakaiHomePath(), evaluationFolderName);
		String evaluationFileName = String.format("%s-%s-evaluation.txt",
				DateTimeFormatter.ofPattern("yyyyMMdd-HH").format(Instant.now().atZone(ZoneId.systemDefault())),
				importPerformance.isCompleted() ? "final" : "intermidiate");
		try {
			File evaluationFolder = Files.createDirectories(evaluationFolderPath).toFile();
			File evaluationFile = Path.of(evaluationFolder.getPath(), evaluationFileName).toFile();

			try (PrintStream out = new PrintStream(new FileOutputStream(evaluationFile))) {
				importPerformance.evaluate(out);
			}

			log.info("Saved evaluation to: {}", evaluationFile.getPath());
		} catch (IOException e) {
			log.error("Could not write to evaluation File {} in {} due to:", evaluationFileName, evaluationFolderPath, e);
		}
	}
}
