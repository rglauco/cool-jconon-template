E' stato posto il tema dell'obbligatorietà dell'allegato dei diritti di segreteria, in quanto, se un candidato volesse iscriversi a pochi minuti dal termine della presentazione delle domande, non sarebbe in grado di allegare per tempo la ricevuta ma potrebbe portarla successivamente in soccorso istruttorio.
In Jconon l'allegato è obbligatorio e questi sono i passi da fare per renderlo facoltativo:
* in console Alfresco disattivare il modello (`deactivate model` *il modello non deve essere mai stato usato*)
* cancellare il file dal workspace e dall'archive di Alfresco

* in `src/main/resources/remote-single-model/Data Dictionary/Models/` caricare il model nuovo con il `mandatory-aspect`:
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
  ~    Copyright (C) 2019  Consiglio Nazionale delle Ricerche
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as
  ~     published by the Free Software Foundation, either version 3 of the
  ~     License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     You should have received a copy of the GNU Affero General Public License
  ~     along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<model xmlns="http://www.alfresco.org/model/dictionary/1.0" name="jconon_pagamenti_diritti_segreteria:jconon_pagamenti_diritti_segreteriaModel"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.alfresco.org/model/dictionary/1.0 modelSchema.xsd">
	<description>JCONON Dynamic Model</description>
	<author>marco.spasiano</author>
	<version>1.2</version>
	<imports>
		<import uri="http://www.alfresco.org/model/dictionary/1.0" prefix="d" />
		<import uri="http://www.alfresco.org/model/content/1.0" prefix="cm" />
		<import uri="http://www.cnr.it/model/jconon_attachment/cmis" prefix="jconon_attachment" />
	</imports>
	<namespaces>
		<namespace uri="http://www.cnr.it/model/jconon_attachment/pagamenti_diritti_segreteria/cmis"
			prefix="jconon_pagamenti_diritti_segreteria" />
	</namespaces>
	<types>
		<type name="jconon_pagamenti_diritti_segreteria:attachment">
			<title>Pagamenti diritti di segreteria</title>
			<description>Pagamenti diritti di segreteria</description>
			<parent>jconon_attachment:document_mono</parent>
			<properties>
				<property name="jconon_pagamenti_diritti_segreteria:estremi_pagamento">
					<title>Estremi pagamento</title>
					<description>Estremi pagamento</description>
					<type>d:text</type>
					<mandatory>false</mandatory>
					<index enabled="true">
						<atomic>true</atomic>
						<stored>false</stored>
						<tokenised>both</tokenised>
					</index>
				</property>
			</properties>
			<mandatory-aspects>
				<aspect>jconon_attachment:document_not_required</aspect>
			</mandatory-aspects>
		</type>
	</types>
</model>
```
* poi, creare il file `D_jconon_pagamenti_diritti_segreteria_attachment.xml` in `src/main/resources/bulkInfo/` e aggiungere `<cmisImplementsName name="P:jconon_attachment:document_not_required" />`, così:

```
<?xml version="1.0" encoding="UTF-8" standalone="no"?><!--
  ~    Copyright (C) 2019  Consiglio Nazionale delle Ricerche
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as
  ~     published by the Free Software Foundation, either version 3 of the
  ~     License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     You should have received a copy of the GNU Affero General Public License
  ~     along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<bulkInfo xmlns="http://www.cnr.it/schema/BulkInfo_v2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" cmisExtendsName="D:jconon_attachment:document_mono" cmisQueryName="jconon_pagamenti_diritti_segreteria:attachment" cmisTypeName="D:jconon_pagamenti_diritti_segreteria:attachment" longDescription="Attachment Pagamento diritti di segreteria" shortDescription="Attachment Pagamento diritti di segreteria" xsi:schemaLocation="http://www.cnr.it/schema/BulkInfo_v2 BulkInfo_v2">

	<fieldProperty inputType="TEXT" label="label.jconon_pagamenti_diritti_segreteria_estremi_pagamento" name="estremi_pagamento" property="jconon_pagamenti_diritti_segreteria:estremi_pagamento"><jsonvalidator required="false"/></fieldProperty>

		
	<formFieldProperty name="messaggio"/> 
	<formFieldProperty class="input-large" name="estremi_pagamento"/>
	<formFieldProperty inputType="HIDDEN" label="" name="title"/>
	<formFieldProperty inputType="HIDDEN" label="" name="description"/>

	<columnFieldProperty class="input-large" name="estremi_pagamento"/>
    <cmisImplementsName name="P:jconon_attachment:document_not_required" />
</bulkInfo>
```
