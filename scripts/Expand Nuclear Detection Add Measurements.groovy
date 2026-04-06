import qupath.lib.analysis.features.ObjectMeasurements
//def serverOriginal = getCurrentServer()
double paramExpansion = 2
double nucScale = -1
img_resolution=getCurrentImageData().getServer().getPixelCalibration().getAveragedPixelSizeMicrons() //Get the current image's resolution
def detections = getDetectionObjects()
def cellBoundaries = [:]
detections.each {it ->
    def roi = it.getROI()
    def geom = roi.getGeometry()
    def boundary = CellTools.estimateCellBoundary(geom,paramExpansion/img_resolution,nucScale)
    cellBoundaries.put(it,boundary)
}
def cells = CellTools.detectionsToCells(cellBoundaries)
//clearCellMeasurements()
//ObjectMeasurements.addShapeMeasurements(cells, serverOriginal.getPixelCalibration())
//cells.each(p -> ObjectMeasurements.addIntensityMeasurements(serverOriginal, p, 1.0, 
//                            ObjectMeasurements.Measurements.values() as List,
//                            ObjectMeasurements.Compartments.values() as List))
removeObjects(detections, true)
addObjects(cells)