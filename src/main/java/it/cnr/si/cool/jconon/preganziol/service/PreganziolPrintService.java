/*
 * Copyright (C) 2021  Consiglio Nazionale delle Ricerche
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.preganziol.service;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.cool.exception.CoolUserFactoryException;
import it.cnr.cool.mail.MailService;
import it.cnr.cool.mail.model.AttachmentBean;
import it.cnr.cool.mail.model.EmailMessage;
import it.cnr.cool.rest.util.Util;
import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.service.I18nService;
import it.cnr.cool.web.scripts.exception.ClientMessageException;
import it.cnr.si.cool.jconon.cmis.model.JCONONPropertyIds;
import it.cnr.si.cool.jconon.service.PrintService;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Primary
@Service
public class PreganziolPrintService extends PrintService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreganziolPrintService.class);
    @Autowired
    private CMISService cmisService;
    @Autowired
    private UserService userService;
    @Autowired
    private I18nService i18nService;
    @Autowired
    private MailService mailService;
    @Autowired
    private ApplicationContext context;

    @Override
    public void printApplication(String nodeRef, final String contextURL, final Locale locale, final boolean email) {
        try {
            LOGGER.info("Start print application width id: " + nodeRef);
            Session cmisSession = cmisService.createAdminSession();
            Folder application = (Folder) cmisSession.getObject(nodeRef);
            Boolean confirmed = isConfirmed(application);
            Folder call = (Folder) cmisSession.getObject(application.getParentId());
            application.refresh();
            CMISUser applicationUser;
            try {
                applicationUser = userService.loadUserForConfirm(
                        application.getPropertyValue(JCONONPropertyIds.APPLICATION_USER.value()));
            } catch (CoolUserFactoryException e) {
                throw new ClientMessageException("User not found of application " + nodeRef, e);
            }
            String nameRicevutaReportModel = getNameRicevutaReportModel(cmisSession, application, locale);
            byte[] stampaByte = getRicevutaReportModel(cmisSession,
                    application, contextURL, nameRicevutaReportModel, false);
            InputStream is = new ByteArrayInputStream(stampaByte);
            archiviaRicevutaReportModel(cmisSession, application, is, nameRicevutaReportModel, confirmed);

            /**
             * Spedisco la mail con la stampa allegata
             */
            if (email) {
                Map<String, Object> mailModel = new HashMap<String, Object>();
                List<String> emailList = new ArrayList<String>();
                emailList.add(applicationUser.getEmail());
                mailModel.put("contextURL", contextURL);
                mailModel.put("folder", application);
                mailModel.put("call", call);
                mailModel.put("message", context.getBean("messageMethod", locale));
                mailModel.put("email_comunicazione", applicationUser.getEmail());
                EmailMessage message = new EmailMessage();
                message.setRecipients(emailList);
                String body;
                if (confirmed) {
                    message.setBccRecipients(Arrays.asList("ced@comune.preganziol.tv.it"));
                    body = Util.processTemplate(mailModel, "/pages/application/application.registration.html.ftl");
                    message.setSubject(	i18nService.getLabel("subject-info", locale) + i18nService.getLabel("subject-confirm-domanda", locale, call.getProperty(JCONONPropertyIds.CALL_CODICE.value()).getValueAsString(), application.getProperty("jconon_application:cognome").getValueAsString(), application.getProperty("jconon_application:nome").getValueAsString() ) );
                    Map<String, Object> properties = new HashMap<String, Object>();
                    properties.put(JCONONPropertyIds.APPLICATION_DUMMY.value(), "{\"stampa_archiviata\" : true}");
                    application.updateProperties(properties);
                } else {
                    body = Util.processTemplate(mailModel, "/pages/application/application.print.html.ftl");
                    message.setSubject(i18nService.getLabel("subject-info", locale) +
                            i18nService.getLabel("subject-print-domanda", locale, call.getProperty(JCONONPropertyIds.CALL_CODICE.value()).getValueAsString()));
                }
                message.setBody(body);
                message.setAttachments(Arrays.asList(new AttachmentBean(nameRicevutaReportModel, stampaByte)));
                mailService.send(message);
            }
            if (LOGGER.isInfoEnabled())
                LOGGER.info("End print application width id: " + nodeRef);
        } catch (Exception t) {
            LOGGER.error("Error while print application width id:" + nodeRef, t);
        }
    }
}
