package mil.army.usace.hec.dss.internal;

import mil.army.usace.hec.dss.*;

import java.util.stream.Stream;

public final class HecDssImpl implements HecDss {
    private final DssSession dssSession;
    private final DssCatalogService dssCatalogService;
    private final DssTimeSeriesService dssTimeSeriesService;

    private HecDssImpl(String dssFileName) {
        dssSession = DssSession.initiate(dssFileName);
        dssCatalogService = new DssCatalogService(dssSession);
        dssTimeSeriesService = new DssTimeSeriesService(dssSession);
    }

    public static HecDss open(String dssFileName) {
        return new HecDssImpl(dssFileName);
    }

    @Override
    public Stream<DssPathname> getCatalog() {
        return dssCatalogService.getCatalog();
    }

    @Override
    public int getRecordCount() {
        return dssCatalogService.getRecordCount();
    }

    @Override
    public DssTimeSeries getTimeSeries(DssPathname pathname) {
        return dssTimeSeriesService.getTimeSeries(pathname);
    }

    @Override
    public DssTimeSeries getTimeSeries(DssPathname pathname, DssTimeWindow timeWindow) {
        return dssTimeSeriesService.getTimeSeries(pathname, timeWindow);
    }

    @Override
    public void close() {
        dssSession.close();
    }
}
