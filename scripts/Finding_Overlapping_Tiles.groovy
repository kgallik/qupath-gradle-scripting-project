def tiles = getTileObjects().findAll
def match = [] as Set  // Use a Set instead of List

tiles.eachWithIndex { tile1, i ->
    def geom1 = tile1.getROI().getGeometry()
    
    for (int j = i + 1; j < tiles.size(); j++) {
        def tile2 = tiles[j]
        def geom2 = tile2.getROI().getGeometry()
        
        if (geom1.intersects(geom2)) {
            def intersection = geom1.intersection(geom2)
            
            if (intersection.getArea() > 0) {
                //println "Found overlapping tiles: ${tile1.getID()} and ${tile2.getID()} (overlap area: ${intersection.getArea()})"
                match << tile1
                match << tile2
            }
        }
    }
}

println "Found ${match.size()} unique overlapping tiles, adding measurements and metadata for filtering"

match.each{it ->
    def measurements = it.getMeasurementList()
    measurements.put('Overlapping_Tile',1)
    measurements.close()
    }
print
def project = getProject()
def entry = project.getEntry(getCurrentImageData())

// Add metadata to the project entry
if (match.size() > 0) {
    entry.putMetadataValue('HasOverlappingTiles', 'Yes')
    entry.putMetadataValue('OverlapCount', match.size().toString())
} else {
    entry.putMetadataValue('HasOverlappingTiles', 'No')
    entry.putMetadataValue('OverlapCount', '0')
}


println "Done!"
