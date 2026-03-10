/**
 * Cellpose Detection Template script
 * @author Olivier Burri
 */
def imageData = getCurrentImageData()
def stain = getProjectEntryMetadataValue('Stain')
def condition = getProjectEntryMetadataValue('Condition')
def anno = getAnnotationObjects()
def i = 0
for (a in anno) {
    a.setName("Oocyte_${i}_${stain}")
    a.setClassification("${condition}")
    i++
}


// Specify the model name (cyto, nuclei, cyto2, ... or a path to your custom model as a string)
// Other models for Cellpose https://cellpose.readthedocs.io/en/latest/models.html
// And for Omnipose: https://omnipose.readthedocs.io/models.html
def pathModel = 'cyto3'
def cellpose = Cellpose2D.builder( pathModel )
        .pixelSize( 0.5*2.8 )                      // Resolution for detection in um
        .channels( 0,1,2 )	               // Select detection channel(s)
//        .tempDirectory( new File( '/tmp' ) )         // Temporary directory to export images to. defaults to 'cellpose-temp' inside the QuPath Project
//        .preprocess( ImageOps.Filters.median( 1 ) )  // List of preprocessing ImageOps to run on the images before exporting them
//        .normalizePercentilesGlobal( 0.1, 99.8, 10 ) // Convenience global percentile normalization. arguments are percentileMin, percentileMax, dowsample. If no channel mentioned, arguments are applied for all selected channels. Otherwise, applied only for the selected channel. See https://forum.image.sc/t/cellpose-sam-qupath-extension/112418/27
//        .tileSize( 1024 )                  // If your GPU can take it, make larger tiles to process fewer of them. Useful for Omnipose
//        .cellposeChannels( 1,2 )           // Overwrites the logic of this plugin with these two values. These will be sent directly to --chan and --chan2
//        .cellprobThreshold( 0.0 )          // Threshold for the mask detection, defaults to 0.0
//        .flowThreshold( 0.4 )              // Threshold for the flows, defaults to 0.4
//        .diameter( 15 )                    // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
//        .useOmnipose()                     // Use omnipose instead
        .useCellposeSAM()                  // Use cellposeSAM (i.e. cellpose 4.x.x) env instead of previous versions of cellpose <= v3.x.x
//        .addParameter( "cluster" )         // Any parameter from cellpose or omnipose not available in the builder.
//        .addParameter( "save_flows" )      // Any parameter from cellpose or omnipose not available in the builder.
//        .addParameter( "anisotropy", "3" ) // Any parameter from cellpose or omnipose not available in the builder.
//        .cellExpansion( 5.0 )              // Approximate cells based upon nucleus expansion
//        .cellConstrainScale( 1.5 )         // Constrain cell expansion using nucleus size
//        .classify( "My Detections" )       // PathClass to give newly created objects
        .measureShape()                    // Add shape measurements
//        .measureIntensity()                // Add cell measurements (in all compartments)
//        .createAnnotations()               // Make annotations instead of detections. This ignores cellExpansion
//        .simplify( 0 )                     // Simplification 1.6 by default, set to 0 to get the cellpose masks as precisely as possible
//        .disableGPU()                      // Force using CPU.
//        .excludeEdges()                    // remove cells touching the border => higher priority than constrainToParent()
//        .constrainToParent(false, 15)      // display all and entirely the cells intersecting the parent annotation, with an optional padding around the parent annotation given in um. Default true and 15um. Ignored if excludeEdges() is called.
        .build()

// Run detection for the selected objects

//def pathObjects = getSelectedObjects() // To process only selected annotations, useful while testing
def pathObjects = getAnnotationObjects() // To process all annotations. For working in batch mode
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage( "Cellpose", "Please select a parent object!" )
    return
}

cellpose.detectObjects( imageData, pathObjects )

// You could do some post-processing here, e.g. to remove objects that are too small, but it is usually better to
// do this in a separate script so you can see the results before deleting anything.

println 'Cellpose detection script done'

import qupath.ext.biop.cellpose.Cellpose2D

def detections = getDetectionObjects()
getCurrentHierarchy().getSelectionModel().selectObjects(detections)
runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin', '{"pixelSizeMicrons":0.5,"region":"ROI","tileSizeMicrons":25.0,"colorOD":true,"colorStain1":false,"colorStain2":false,"colorStain3":false,"colorRed":false,"colorGreen":false,"colorBlue":false,"colorHue":false,"colorSaturation":false,"colorBrightness":false,"doMean":true,"doStdDev":true,"doMinMax":true,"doMedian":true,"doHaralick":false,"haralickDistance":1,"haralickBins":32}')