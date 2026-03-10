def tissues = getAnnotationObjects()
getCurrentHierarchy().getSelectionModel().selectObjects(tissues);

// APPLY CELL CLASSIFIER DEPENDING ON MARKER
def model = getProjectEntryMetadataValue('Model')

if (model == 'Gen_Human_Only') {
    println "Running Generalized Model Human Only Training"
    addPixelClassifierMeasurements("Generalized_Classifier_V3_Human_Only", "Generalized_Classifier_V3_Human_Only");
} else if (model == 'Gen_Rat_Only') {
    println "Running Generalized Model Rat Only Training"
    addPixelClassifierMeasurements("Generalized_Classifier_V3_Rat_Only", "Generalized_Classifier_V3_Rat_Only");
} else if (model == 'Gen_V3') {
    println "Running Generalized Model"
    addPixelClassifierMeasurements("Generalized_Classifier_V3", "Generalized_Classifier_V3");
} else {
    println "Running Original Rat Model"
    addPixelClassifierMeasurements("Mammary_Gland_Pixel_Classifier_V4", "Mammary_Gland_Pixel_Classifier_V4");    
}


//Add to end of any script running in batch to help clear up memory space. Very important for running batch scripts on HPC
Thread.sleep(100)
// Try to reclaim whatever memory we can, including emptying the tile cache
javafx.application.Platform.runLater {
    getCurrentViewer().getImageRegionStore().cache.clear()
    System.gc()
}
Thread.sleep(100)