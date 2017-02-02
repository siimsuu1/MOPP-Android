/*
 * Copyright 2017 Riigi Infosüsteemide Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package ee.ria.EstEIDUtility.mid;

import android.util.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ee.ria.EstEIDUtility.container.ContainerFacade;
import ee.ria.EstEIDUtility.container.DataFileFacade;
import ee.ria.libdigidocpp.DataFile;
import ee.ria.libdigidocpp.Signature;
import ee.ria.mopp.androidmobileid.dto.request.DataFileDto;
import ee.ria.mopp.androidmobileid.dto.request.MobileCreateSignatureRequest;

public class CreateSignatureRequestBuilder {

    private static final String FORMAT = "BDOC";
    private static final String VERSION = "2.1";
    private static final String MESSAGING_MODE = "asynchClientServer";
    private static final String SERVICE_NAME = "DigiDoc3";
    private static final int ASYNC_CONFIGURATION = 0;
    private static final String DIGEST_TYPE = "sha256";
    private static final String DIGEST_METHOD = "http://www.w3.org/2001/04/xmlenc#sha256";
    private static final String DEFAULT_LANGUAGE = "ENG";
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("ENG", "EST", "RUS", "LIT");

    private String phoneNr;
    private String idCode;
    private String message;
    private Locale locale;
    private String localSigningProfile;
    private ContainerFacade container;
    private MobileCreateSignatureRequest request;

    private CreateSignatureRequestBuilder() {}

    public MobileCreateSignatureRequest build() {
        request = new MobileCreateSignatureRequest();
        buildConstantParameters();
        buildPersonalInfo();
        buildContainerParameters();
        return request;
    }

    private void buildPersonalInfo() {
        request.setIdCode(idCode);
        request.setPhoneNr(phoneNr);
        request.setLanguage(getLanguage());
        request.setMessageToDisplay(message == null ? "Sign " + container.getName() : message);
    }

    private String getLanguage() {
        if (locale == null) {
            return DEFAULT_LANGUAGE;
        } else {
            return getLanguageFromLocaleOrDefault(locale);
        }
    }

    private String getLanguageFromLocaleOrDefault(Locale locale) {
        try {
            String language = locale.getISO3Language().toUpperCase();
            return SUPPORTED_LANGUAGES.contains(language) ? language : DEFAULT_LANGUAGE;
        } catch (Exception e) {
            return DEFAULT_LANGUAGE;
        }
    }

    private void buildConstantParameters() {
        request.setFormat(FORMAT);
        request.setVersion(VERSION);
        request.setMessagingMode(MESSAGING_MODE);
        request.setServiceName(SERVICE_NAME);
        request.setAsyncConfiguration(ASYNC_CONFIGURATION);
    }

    private void buildContainerParameters() {
        request.setSigningProfile(getSigningProfile());
        request.setSignatureId(getNextSignatureId());
        buildDataFiles();
    }

    private void buildDataFiles() {
        List<DataFileFacade> dataFiles = container.getDataFiles();
        List<DataFileDto> dataFileDtos = new ArrayList<>();
        for (DataFileFacade df : dataFiles) {
            dataFileDtos.add(createDataFileDto(df));
        }
        request.setDatafiles(dataFileDtos);
    }

    private DataFileDto createDataFileDto(DataFileFacade df) {
        DataFileDto dto = new DataFileDto();
        dto.setId(df.getId());
        dto.setMimeType(df.getMediaType());
        dto.setDigestType(DIGEST_TYPE);
        dto.setDigestValue(Base64.encodeToString(df.calcDigest(DIGEST_METHOD), Base64.DEFAULT));
        return dto;
    }

    private String getNextSignatureId() {
        return container.getNextSignatureId();
    }

    private String getSigningProfile() {
        return parseLibdigidocProfile(localSigningProfile);
    }

    private String parseLibdigidocProfile(String profile) {
        if ("time-stamp".equals(profile)) {
            return SigningProfile.LT.name();
        } else {
            return SigningProfile.LT_TM.name();
        }
    }

    public static CreateSignatureRequestBuilder aCreateSignatureRequest() {
        return new CreateSignatureRequestBuilder();
    }

    public CreateSignatureRequestBuilder withLocalSigningProfile(String signingProfile) {
        this.localSigningProfile = signingProfile;
        return this;
    }

    public CreateSignatureRequestBuilder withPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
        return this;
    }

    public CreateSignatureRequestBuilder withIdCode(String idCode) {
        this.idCode = idCode;
        return this;
    }

    public CreateSignatureRequestBuilder withContainer(ContainerFacade container) {
        this.container = container;
        return this;
    }

    public CreateSignatureRequestBuilder withMessageToDisplay(String message) {
        this.message = message;
        return this;
    }

    public CreateSignatureRequestBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public enum SigningProfile {
        LT,
        LT_TM
    }
}