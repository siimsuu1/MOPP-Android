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

package ee.ria.EstEIDUtility.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

import ee.ria.EstEIDUtility.R;
import ee.ria.EstEIDUtility.container.ContainerBuilder;
import ee.ria.EstEIDUtility.container.ContainerFacade;
import ee.ria.EstEIDUtility.fragment.ContainerDetailsFragment;
import ee.ria.EstEIDUtility.fragment.ErrorOpeningFileFragment;
import ee.ria.EstEIDUtility.util.Constants;
import ee.ria.EstEIDUtility.util.FileUtils;

public class OpenExternalFileActivity extends EntryPointActivity {

    public static final String TAG = OpenExternalFileActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_file);
        handleIntentAction();
    }

    private void handleIntentAction() {
        Intent intent = getIntent();

        ContainerFacade container = null;
        switch (intent.getAction()) {
            case Intent.ACTION_VIEW:
                try {
                    container = createContainer(intent.getData());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            case Intent.ACTION_SEND:
                Uri sendUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (sendUri != null) {
                    container = createContainer(sendUri);
                }
                break;
            case Intent.ACTION_SEND_MULTIPLE:
                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (uris != null) {
                    container = createContainer(uris);
                }
                break;
        }

        if (container != null) {
            createContainerDetailsFragment(container);
        } else {
            createErrorFragment();
        }
    }

    private ContainerFacade createContainer(List<Uri> uris) {
        if (!uris.isEmpty()) {
            List<Uri> containers = searchForContainers(uris);
            if (!containers.isEmpty()) {
                Uri containerUri = containers.get(0);
                uris.remove(containerUri);
                return ContainerBuilder
                        .aContainer(this)
                        .fromExistingContainer(containerUri)
                        .withDataFiles(uris)
                        .build();
            }

            String fileName = FileUtils.resolveFileName(uris.get(0), getContentResolver());
            return ContainerBuilder
                    .aContainer(this)
                    .withDataFiles(uris)
                    .withContainerLocation(ContainerBuilder.ContainerLocation.CACHE)
                    .withContainerName(FilenameUtils.getBaseName(fileName) + "." + Constants.BDOC_EXTENSION)
                    .build();
        }
        return null;
    }

    public ContainerFacade createContainer(Uri uri) {
        String fileName = FileUtils.resolveFileName(uri, getContentResolver());
        if (FileUtils.isContainer(fileName)) {
            return ContainerBuilder
                    .aContainer(this)
                    .fromExistingContainer(uri)
                    .build();
        } else {
            return ContainerBuilder
                    .aContainer(this)
                    .withDataFile(uri)
                    .withContainerLocation(ContainerBuilder.ContainerLocation.CACHE)
                    .withContainerName(FilenameUtils.getBaseName(fileName) + "." + Constants.BDOC_EXTENSION)
                    .build();
        }
    }

    private List<Uri> searchForContainers(List<Uri> uris) {
        List<Uri> containers = new ArrayList<>();
        for (Uri uri : uris) {
            if (FileUtils.isContainer(FileUtils.resolveFileName(uri, getContentResolver()))) {
                containers.add(uri);
            }
        }
        return containers;
    }

    private void createContainerDetailsFragment(ContainerFacade container) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ContainerDetailsFragment containerDetailsFragment = findContainerDetailsFragment();
        if (containerDetailsFragment != null) {
            return;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle extras = new Bundle();

        extras.putString(Constants.CONTAINER_NAME_KEY, container.getName());
        extras.putString(Constants.CONTAINER_PATH_KEY, container.getAbsolutePath());

        containerDetailsFragment = new ContainerDetailsFragment();
        setTitle(R.string.bdoc_detail_title);
        containerDetailsFragment.setArguments(extras);
        fragmentTransaction.add(R.id.container_layout_holder, containerDetailsFragment, ContainerDetailsFragment.TAG);
        fragmentTransaction.commit();
    }

    private void createErrorFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ErrorOpeningFileFragment errorFragment = findErrorFragment();
        if (errorFragment != null) {
            return;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        errorFragment = new ErrorOpeningFileFragment();
        setTitle(R.string.error_page_title);
        fragmentTransaction.add(R.id.container_layout_holder, errorFragment, ErrorOpeningFileFragment.TAG);
        fragmentTransaction.commit();
    }

    private ErrorOpeningFileFragment findErrorFragment() {
        return (ErrorOpeningFileFragment) getSupportFragmentManager().findFragmentByTag(ErrorOpeningFileFragment.TAG);
    }

    private ContainerDetailsFragment findContainerDetailsFragment() {
        return (ContainerDetailsFragment) getSupportFragmentManager().findFragmentByTag(ContainerDetailsFragment.TAG);
    }
}
