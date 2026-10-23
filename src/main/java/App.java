import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String XSD = "src/main/resources/cinema.xsd";
    private static final String XML_VALIDE = "src/main/resources/cinema_valide.xml";
    private static final String XML_INVALIDE = "src/main/resources/cinema_invalide.xml";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  ÉTAPE 1 : VALIDATION XML/XSD ");
        System.out.println("=================================================");
        validateXML(XML_VALIDE, XSD);
        validateXML(XML_INVALIDE, XSD);

        System.out.println("\n=================================================");
        System.out.println("  ÉTAPE 2 : INTERROGATION XPATH ");
        System.out.println("=================================================");
        queryXPath(XML_VALIDE);
    }

    private static void validateXML(String xmlPath, String xsdPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();

            // Liste pour stocker toutes les erreurs
            final List<String> errors = new ArrayList<>();

            // Gestionnaire pour ne pas s'arrêter à la première erreur
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) { }

                @Override
                public void error(SAXParseException exception) {
                    errors.add(exception.getMessage());
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    errors.add(exception.getMessage());
                    throw exception;
                }
            });

            validator.validate(new StreamSource(new File(xmlPath)));

            if (errors.isEmpty()) {
                System.out.println("[VALIDE] Le fichier " + xmlPath + " respecte les contraintes.");
            } else {
                System.out.println("[INVALIDE] Le fichier " + xmlPath + " contient " + errors.size() + " erreur(s) :");
                for (String err : errors) {
                    System.out.println(" -> " + err);
                }
            }

        } catch (SAXException e) {
            System.out.println("[ERREUR FATALE] Lecture impossible : " + xmlPath);
        } catch (IOException e) {
            System.out.println("[ERREUR IO] Impossible de lire " + xmlPath);
        }
    }

    private static void queryXPath(String xmlPath) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new File(xmlPath));
            XPath xpath = XPathFactory.newInstance().newXPath();

            // 1. Clients ayant réservé un film bien noté
            System.out.println("Requête 1 : Clients ayant réservé pour un film (> 4.5)");
            String q1 = "//clientVIP[reservations/reservation/@seanceRef = " +
                    "//seance[@filmRef = //film[note > 4.5]/@id]/@id]/nom | " +
                    "//client[reservations/reservation/@seanceRef = " +
                    "//seance[@filmRef = //film[note > 4.5]/@id]/@id]/nom";
            NodeList clients = (NodeList) xpath.evaluate(q1, doc, XPathConstants.NODESET);
            for (int i = 0; i < clients.getLength(); i++) {
                System.out.println(" - " + clients.item(i).getTextContent());
            }

            // 2. Calcul du prix avec promotion
            System.out.println("\nRequête 2 : Prix final de 'Inception'");
            String q2 = "//film[titre='Inception']/prixBase - (//film[titre='Inception']/prixBase * //film[titre='Inception']/promotion/pourcentage div 100)";
            String prixFinal = xpath.evaluate(q2, doc);
            System.out.println(" - Prix final : " + prixFinal + " €");

            // 3. Salles avec Dolby Atmos
            System.out.println("\nRequête 3 : Salles avec 'Dolby Atmos'");
            String q3 = "//salle[equipements/equipement='Dolby Atmos']/nom | //salle[equipements/equipement='Dolby Atmos']/capacite";
            NodeList salles = (NodeList) xpath.evaluate(q3, doc, XPathConstants.NODESET);
            for (int i = 0; i < salles.getLength(); i++) {
                System.out.println(" - " + salles.item(i).getTextContent());
            }

            // 4. Séances des films de Nolan
            System.out.println("\nRequête 4 : Séances des films de Nolan");
            String q4 = "//seance[@filmRef = //film[realisateur='Christopher Nolan']/@id]/date";
            NodeList seances = (NodeList) xpath.evaluate(q4, doc, XPathConstants.NODESET);
            for (int i = 0; i < seances.getLength(); i++) {
                System.out.println(" - Date : " + seances.item(i).getTextContent());
            }

            // 5. Avis 5 étoiles
            System.out.println("\nRequête 5 : Avis 5 étoiles");
            String q5 = "//avis[etoiles=5.0]/commentaire";
            String avis = xpath.evaluate(q5, doc);
            System.out.println(" - \"" + avis + "\"");

        } catch (Exception e) {
            System.out.println("Erreur XPath : " + e.getMessage());
        }
    }
}