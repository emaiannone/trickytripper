package de.koelle.christian.trickytripper.controller;

import android.app.Activity;

import java.io.File;
import java.util.List;

import de.koelle.christian.trickytripper.model.ExportSettings;
import de.koelle.christian.trickytripper.model.ExportSettings.ExportOutputChannel;
import de.koelle.christian.trickytripper.model.Participant;

public interface ExportController {

    ExportSettings getDefaultExportSettings();

    List<File> exportReport(ExportSettings settings, Participant selectedParticipant, Activity activity);

    List<ExportOutputChannel> getEnabledExportOutputChannel();

    boolean hasEnabledOutputChannel();

    boolean osSupportsOpenCsv() ;
    boolean osSupportsOpenTxt() ;
    boolean ossSupportsOpenHtml();
}
