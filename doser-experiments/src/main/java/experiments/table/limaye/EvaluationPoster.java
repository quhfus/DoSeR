package experiments.table.limaye;

public class EvaluationPoster {
//
//	public static int sum = 0;
//
//	public static int correct = 0;
//
//	public static int haveoneresult = 0;
//
//	public static Model model;
//
//	public static void main(String[] args) {
//
//		List<Table> tables = new LinkedList<Table>();
//		File file = new File("/home/quh/Arbeitsfläche/To/wikitables/");
//		File[] f = file.listFiles();
//		for (int u = 0; u < f.length; u++) {
//			System.out.println(f[u].getAbsolutePath());
//			EvaluationPoster eval = new EvaluationPoster();
//			String sourcePath = f[u].getAbsolutePath();
//			String[] splitter = sourcePath.split("/");
//			Table t = eval.readTable(f[u].getAbsolutePath());
//			// t.setName(f[u].getAbsolutePath());
//			File gtf = new File("/home/quh/Arbeitsfläche/gt/wikitables/"
//					+ splitter[splitter.length - 1]);
//			eval.addGT(t, gtf.getAbsolutePath());
//			tables.add(t);
//		}
//		model = null;
//		try {
//			HDT hdt = HDTManager.mapIndexedHDT("/home/quh/HDT/yagoTypes.hdt",
//					null);
//			HDTGraph graph = new HDTGraph(hdt);
//			model = ModelFactory.createModelForGraph(graph);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		for (int i = 0; i < tables.size(); i++) {
//			EvaluationPoster eval = new EvaluationPoster();
//			Table t = tables.get(i);
//			eval.disambiguate(t);
//
//			EvaluationPoster.evaluateResults(t);
//		}
//
//		System.out.println("Insgesamt: " + sum + " davon richtig: " + correct);
//	}
//
//	public void disambiguate(Table t) {
//		int nrCols = t.getNumberofColumns();
//		for (int i = 0; i < nrCols; i++) {
//			if (t.getColumn(i).getTypeGt() != null
//					&& t.getColumn(i).getTypeGt().size() > 0) {
//				String jsonResult = "";
//				Gson gson = new Gson();
//				List<Cell> cList = t.getColumn(i).getCellList();
//				List<String> cells = new LinkedList<String>();
//				for (int j = 0; j < cList.size(); j++) {
//					cells.add(cList.get(j).getCellContent());
//				}
//				String cline = "";
//				for (int j = 0; j < cells.size(); j++) {
//					cline += cells.get(j);
//					if ((j + 1) != cells.size()) {
//						cline += ";";
//					} else {
//						cline += " ";
//					}
//				}
//				NormalCallup callup = new NormalCallup();
//				jsonResult = callup.disambiguateTable("", "", "", cline, "",
//						"", "disservice", "", "", "", "", "", "");
//				needForPosterTest.ClassInformationExtendedOutput c = gson
//						.fromJson(
//								jsonResult,
//								needForPosterTest.ClassInformationExtendedOutput.class);
//				List<ClassInformation> infos = c.getEntities();
//				List<String> columnTypes = new LinkedList<String>();
//				if (infos.size() > 0) {
//					columnTypes.add(infos.get(0).getId());
//					t.getColumn(i).setColumnTypes(columnTypes);
//
//				}
//			}
//		}
//	}
//
//	public Table readTable(String uri) {
//		Table t = null;
//		try {
//			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//			FileReader reader = new FileReader(uri);
//			InputSource inputSource = new InputSource(reader);
//			LimayeAnnotationParserWikiTables p = new LimayeAnnotationParserWikiTables();
//			xmlReader.setContentHandler(p);
//			xmlReader.parse(inputSource);
//			t = p.getTable();
//			p = null;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		}
//		return t;
//	}
//
//	public void addGT(Table table, String uri) {
//		try {
//			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//			FileReader reader = new FileReader(uri);
//			InputSource inputSource = new InputSource(reader);
//			LimayeGroundtruthAnnotationParser p = new LimayeGroundtruthAnnotationParser(
//					table);
//			xmlReader.setContentHandler(p);
//			xmlReader.parse(inputSource);
//			p = null;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void evaluateResults(Table t) {
//		// System.out.println(t.getName());
//		int nrC = t.getNumberofColumns();
//		for (int i = 0; i < nrC; i++) {
//			Table.Column c = t.getColumn(i);
//			if (c.getTypeGt() != null && c.getTypeGt().size() > 0) {
//				System.out.println("HALLLLLLLO" + c.getColumnsTypes().get(0));
//				List<String> gt = c.getTypeGt();
//				boolean isIn = false;
//				for (int j = 0; j < gt.size(); j++) {
//					String type = c.getColumnsTypes().get(0);
//					if (type != null) {
//						if (type.equalsIgnoreCase(gt.get(j))) {
//							isIn = true;
//						}
//					}
//				}
//				if (!gt.get(0).equalsIgnoreCase("null")
//						&& gt.get(0) != null) {
//					if (isIn) {
//						correct++;
//					} else {
//						if (gt.size() > 0) {
//							System.out.println("Not found" + gt.get(0));
//						}
//					}
//					sum++;
//				}
//			}
//		}
//		System.out.println("Insgesamt: " + sum + " davon richtig: " + correct);
//	}

}
