import qupath.lib.objects.PathObjects
import qupath.lib.roi.ROIs

// Get all cells and point annotations
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()
def cells = getCellObjects()
// Get all point annotation objects
def pointAnnotations = getAnnotationObjects().findAll { it.getROI().isPoint() }
// Extract all individual points from each annotation
def allPoints = []
pointAnnotations.each { annotation ->
    def roi = annotation.getROI()
    def pointsList = roi.getAllPoints()  // Returns a list of Point2 objects
    pointsList.each { point ->
        allPoints << [
            x: point.getX(),
            y: point.getY(),
            pathClass: annotation.getPathClass()  // Inherit class from parent annotation
        ]
    }
}

// Empty lists for predictions and labels
def predictions = []
def labels = []

//for (point in points) {
//        def pointROI = point.getROI()
//        println pointROI.getCentroidX()
//}

// Loop through all cells
println "Collecting values for predictions and ground truth labels"
cells.each { cell ->
    def cellROI = cell.getROI()
    
    // Get prediction (0 or 1 based on PathClass)
    def pathClass = cell.getPathClass()
    def pred = (pathClass != null && pathClass.getName() == "YAP") ? 1 : 0
    //println pred
    predictions << pred
    
    // Check if cell contains any point (ground truth)
    def groundTruth = null  // default is unlabeled
    
    for (point in allPoints) {
        // Check if point is inside cell ROI
        if (cellROI.contains(point.x, point.y)) {
            def pointClass = point.pathClass
            
            if (pointClass != null) {
                if (pointClass.getName() == "YAP") {
                    groundTruth = 1
                } else if (pointClass.getName().startsWith("Ignore")) {
                    groundTruth = 0
                }
            }
            break  // Found a point in this cell, stop searching
        }
    }
    //println groundTruth
    labels << groundTruth
}
println "Done collecting values, calculating confusion matrix!"
// Calculate confusion matrix (only on labeled data)
def calculateConfusionMatrix(actualValues, predictedValues) {
    def tp = 0, tn = 0, fp = 0, fn = 0
    def unlabeled = 0
    
    actualValues.eachWithIndex { actual, i ->
        def predicted = predictedValues[i]
        
        if (actual == null) {
            unlabeled++
        } else if (actual == 1 && predicted == 1) {
            tp++
        } else if (actual == 0 && predicted == 0) {
            tn++
        } else if (actual == 0 && predicted == 1) {
            fp++
        } else if (actual == 1 && predicted == 0) {
            fn++
        }
    }
    
    return [tp: tp, tn: tn, fp: fp, fn: fn, unlabeled: unlabeled]
}

def calculateMetrics(matrix) {
    def labeled = matrix.tp + matrix.tn + matrix.fp + matrix.fn
    
    if (labeled == 0) {
        println "ERROR: No labeled data available"
        return null
    }
    
    def accuracy = (matrix.tp + matrix.tn) / labeled
    def precision = (matrix.tp + matrix.fp) > 0 ? matrix.tp / (matrix.tp + matrix.fp) : 0
    def recall = (matrix.tp + matrix.fn) > 0 ? matrix.tp / (matrix.tp + matrix.fn) : 0
    def f1 = (precision + recall) > 0 ? 2 * (precision * recall) / (precision + recall) : 0
    def specificity = (matrix.tn + matrix.fp) > 0 ? matrix.tn / (matrix.tn + matrix.fp) : 0
    
    return [
        accuracy: accuracy,
        precision: precision,
        recall: recall,
        f1: f1,
        specificity: specificity,
        labeled: labeled,
        unlabeled: matrix.unlabeled
    ]
}

// Calculate and display results
def matrix = calculateConfusionMatrix(labels, predictions)
def metrics = calculateMetrics(matrix)

if (metrics != null) {
    println "\n=== Confusion Matrix ==="
    println "                Predicted"
    println "                Negative       Positive"
    println "Actual  Negative      ${matrix.tn}      ${matrix.fp}"
    println "        Positive      ${matrix.fn}      ${matrix.tp}"
    println "\nUnlabeled cells: ${matrix.unlabeled}"
    println "Labeled cells: ${metrics.labeled}"
    println "Total cells: ${cells.size()}"
    
    println "\n=== Metrics (on labeled data only) ==="
    println "Accuracy:    ${String.format('%.3f', metrics.accuracy)}"
    println "Precision:   ${String.format('%.3f', metrics.precision)}"
    println "Recall:      ${String.format('%.3f', metrics.recall)}"
    println "F1 Score:    ${String.format('%.3f', metrics.f1)}"
    println "Specificity: ${String.format('%.3f', metrics.specificity)}"
    println "Coverage:    ${String.format('%.1f%%', 100.0 * metrics.labeled / cells.size())}"
}

println "\nDone!"