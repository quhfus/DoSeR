package experiments.webclassify.firstexperiments;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.StringReader;
//
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import org.xml.sax.XMLReader;
//import org.xml.sax.helpers.XMLReaderFactory;
//
//import doser.webclassify.categoryannotation.AnnotateCategories;
//import doser.webclassify.dpo.WebSite;
//
//public class AnnotateSinglePages {
//
//	public static final String PAGE = "/home/quh/Arbeitsfläche/Alan_Turing.html";
//	
//	private AnnotateCategories algorithm;
//	
//	public AnnotateSinglePages() {
//		super();
//		algorithm = new AnnotateCategories();
//	}
//	
//	public void doExperiment() {
//		WebSite website = new WebSite();
//		website.setName("Alan Turing");
//		website.setObjectId(0);
//		File cFile = new File(PAGE);
//		String content = "";
//		try {
//			BufferedReader reader = new BufferedReader(
//					new FileReader(cFile));
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				content += line;
//			}
//			reader.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
//			ExtractWikipediaText handler = new ExtractWikipediaText();
//			InputSource inputSource = new InputSource(new StringReader(
//					content));
//			xmlReader.setContentHandler(handler);
//			xmlReader.parse(inputSource);
//			String text = handler.getDocumentText();
//			website.setText(text);
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		algorithm.annotateCategory(website);
//	}
//	
//	public static void main(String[] args) {
//		AnnotateSinglePages pages = new AnnotateSinglePages();
//		pages.doExperiment();
//	}
//}
