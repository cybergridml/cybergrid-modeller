# Illustration des fonctionnalités de CyberGridML

Cette section présente les principales fonctionnalités de **CyberGridML** à travers un cas d'étude basé sur une architecture **SCADA**.

---

## 1. Création d'un projet CyberGridML

CyberGridML fournit un modèle de projet (*template*) construit sur **SysON**.

![Template CyberGridML basé sur SysON](images/cybergridml_template.png)

Tout au long de cette démonstration, nous nous appuyons sur un modèle **SCADA** afin d'illustrer les différentes fonctionnalités offertes par CyberGridML.


---

## 2. Création du CFA

La palette de création permet d'ajouter les différents éléments constituant un **Cyber Functional Architecture (CFA)**.

![Palette de création du CFA](images/cfa_palette.png)

La figure suivante présente un exemple complet de CFA.

![Exemple complet de CFA](images/cfa_complete.png)

---

## 3. Création d'un CRA

La figure suivante met en évidence la palette dédiée à la création d'un **Cyber Risk Assessment (CRA)**.

![Palette du CRA](images/cra_palette.png)

L'exemple ci-dessous présente un CRA construit à partir du cas d'étude **SCADA**.

![CRA basé sur le cas d'étude SCADA](images/cra_scada.png)

Les propriétés associées sont automatiquement renseignées dans l'onglet **NACRE** de la vue **Properties**.

![Vue Properties - Onglet NACRE](images/nacre_properties_1.png)

![Détails des propriétés NACRE](images/nacre_properties_2.png)

---

## 4. Application des Hazards

L'application d'un **Hazard** s'effectue en sélectionnant dans un premier temps l'élément concerné.

![Sélection de l'élément](images/hazard_selection.png)

Une fois l'élément sélectionné, le Hazard peut être appliqué.

![Application du Hazard](images/hazard_application.png)

Après son application, l'élément est mis en évidence par une coloration rouge.

![Élément marqué après application du Hazard](images/hazard_marked_element.png)

Les propriétés associées au Hazard sont alors automatiquement ajoutées à l'élément concerné.

![Propriétés du Hazard](images/hazard_properties.png)