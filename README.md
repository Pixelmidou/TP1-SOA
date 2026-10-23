# TP1 SOA — Gestion de cinéma (XML, XSD, XPath, Maven)

## Domaine choisi
Le domaine choisi est la **gestion de cinéma**. Il permet de modéliser des films, des salles, des séances et des clients avec leurs réservations.

## Arborescence du projet

```text
TP1-SOA/
├── .idea/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── App.java
│   │   └── resources/
│   │       ├── cinema.xsd
│   │       ├── cinema_invalide.xml
│   │       └── cinema_valide.xml
│   └── test/
├── .gitignore
├── pom.xml
└── README.md
```

## Détails du Schéma XSD (Fonctionnalités Avancées)
Le schéma utilise des mécanismes avancés pour garantir une structure de données parfaite :
- **`xs:key` et `xs:keyref`** : Implémentation de clés primaires et étrangères. Une `<seance>` ne peut pas référencer un `filmRef` qui n'existe pas dans la liste des `<films>`.
- **`xs:choice`** : Le bloc `<promotion>` d'un film accepte *soit* un `<pourcentage>`, *soit* un `<montantFixe>`.
- **`xs:extension` (Héritage)** : Le type `ClientVIPType` hérite du `ClientBaseType` et y ajoute un `<numeroCarteVIP>` et des réservations.
- **`xs:group` et `xs:attributeGroup`** : L'adresse (`rue`, `ville`, `codePostal`) et l'attribut `id` sont factorisés pour être réutilisés dans plusieurs types.
- **`simpleType` stricts** : Validation par expressions régulières (`xs:pattern` pour l'email) et énumérations (`xs:enumeration` pour le genre et l'abonnement).
- **Collecte exhaustive des erreurs** : Le code Java utilise un `ErrorHandler` personnalisé pour capturer **toutes** les erreurs du fichier XML invalide en une seule passe, au lieu de s'arrêter à la première.

## Requêtes XPath Avancées utilisées

| Objectif | Complexité & Explication | Requête XPath |
|---|---|---|
| **1. Jointure multiple** | *Clients ayant réservé pour un film > 4.5*. Fait une jointure entre `clientVIP`/`client` -> `reservation` -> `seance` -> `film`. | `//clientVIP[reservations/reservation/@seanceRef = //seance[@filmRef = //film[note > 4.5]/@id]/@id]/nom \| //client[...]/nom` |
| **2. Calcul mathématique** | *Prix final calculé (Base - %Promo)*. Utilise la division `div 100` et la soustraction directement dans XPath. | `//film[titre='Inception']/prixBase - (//film[titre='Inception']/prixBase * //film[titre='Inception']/promotion/pourcentage div 100)` |
| **3. Éléments imbriqués** | *Salles avec Dolby Atmos*. Navigue dans la liste profonde `equipements/equipement`. | `//salle[equipements/equipement='Dolby Atmos']/nom \| //salle[equipements/equipement='Dolby Atmos']/capacite` |
| **4. Jointure par Clé** | *Séances des films de Nolan*. Utilise l'attribut `filmRef` pour lier une séance à son réalisateur. | `//seance[@filmRef = //film[realisateur='Christopher Nolan']/@id]/date` |
| **5. Filtre sur Avis** | *Avis parfaits (5 étoiles)*. Filtre les commentaires basés sur leur note imbriquée. | `//avis[etoiles=5.0]/commentaire` |

## Exécution

```bash
mvn clean compile
mvn exec:java
```

Ou exécuter directement la classe `App` dans IntelliJ IDEA.

## Résultat obtenu

```text
=================================================
  ÉTAPE 1 : VALIDATION XML/XSD AVANCÉE 
=================================================
[VALIDE] Le fichier src/main/resources/cinema_valide.xml respecte les contraintes du schéma.
[INVALIDE] Le fichier src/main/resources/cinema_invalide.xml contient 9 erreur(s) :
 -> cvc-complex-type.4: Attribute 'id' must appear on element 'film'.
 -> cvc-enumeration-valid: Value 'Horreur' is not facet-valid with respect to enumeration '[Science-fiction, Drame, Action, Comédie]'. It must be a value from the enumeration.
 -> cvc-type.3.1.3: The value 'Horreur' of element 'genre' is not valid.
 -> cvc-maxInclusive-valid: Value '6.5' is not facet-valid with respect to maxInclusive '5.0' for type 'NoteType'.
 -> cvc-type.3.1.3: The value '6.5' of element 'note' is not valid.
 -> cvc-identity-constraint.4.2.1.a: Element "cinema" has no value for the key "filmKey".
 -> cvc-pattern-valid: Value 'hela-at-example.com' is not facet-valid with respect to pattern '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}' for type 'EmailType'.
 -> cvc-type.3.1.3: The value 'hela-at-example.com' of element 'email' is not valid.
 -> cvc-identity-constraint.4.3: Key 'seanceFilmRef' with value 'F999' not found for identity constraint of element 'cinema'.

=================================================
  ÉTAPE 2 : INTERROGATION XPATH DE HAUT NIVEAU 
=================================================
Requête 1 : Clients (VIP ou standard) ayant réservé une place pour un film bien noté (> 4.5)
 - Amel

Requête 2 : Calcul du prix final du film 'Inception' (Prix de base - Promotion)
 - Prix final calculé : 12 €

Requête 3 : Salles équipées de 'Dolby Atmos' et leur capacité
 - Salle IMAX 1
 - 300

Requête 4 : Séances programmées pour les films de Christopher Nolan
 - Date de séance : 2025-12-20

Requête 5 : Avis ayant donné 5 étoiles
 - "Un chef d'oeuvre absolu."
```