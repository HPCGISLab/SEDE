package data;

public class MapDataRequestPojo {
    private SpatialDataPojo spatialdata;
    private TemporalDataPojo temporaldata;
    private TextualDataPojo textualdata;
    private DictDataPojo dictdata;
    public DictDataPojo getDictdata() {
		return dictdata;
	}
	public void setDictdata(DictDataPojo dictdata) {
		this.dictdata = dictdata;
	}
	//private HexMapDataPojo hexmapcenters;
	public SpatialDataPojo getSpatialdata() {
		return spatialdata;
	}
	public TemporalDataPojo getTemporaldata() {
		return temporaldata;
	}
	public TextualDataPojo getTextualdata() {
		return textualdata;
	}
	
	/*public HexMapDataPojo getHexmapcenters() {
		return hexmapcenters;
	}*/
	/*public void setHexmapcenters(HexMapDataPojo hexmapcenters) {
		this.hexmapcenters = hexmapcenters;
	}*/
	public void setSpatialdata(SpatialDataPojo spatialdata) {
		this.spatialdata = spatialdata;
	}
	public void setTemporaldata(TemporalDataPojo temporaldata) {
		this.temporaldata = temporaldata;
	}
	public void setTextualdata(TextualDataPojo textualdata) {
		this.textualdata = textualdata;
	}
}
