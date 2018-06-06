import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class GCodeExporter {
  final Plotter plotter;
  final int plotterAreaWidth;
  final int plotterAreaHeight;
  final float MARGIN = 0.2F;
  final float ANTI_MARGIN = 1 - MARGIN;
  final int UP = 5;
  final int CANVAS_DOWN = -1;
  final int BUCKET_DOWN = -2;
  GCodeExporter(Plotter plotter, int plotterAreaWidth, int plotterAreaHeight){
    this.plotter = plotter;
    this.plotterAreaWidth = plotterAreaWidth;
    this.plotterAreaHeight = plotterAreaHeight;
  }
  void export(File file) {
    BufferedWriter writer = null;
    System.out.println("Exporting to " + file.getAbsolutePath());
    try {
      writer = new BufferedWriter(new FileWriter(file));
      go(writer, 0, 0, UP);
      for(PlotterCommand plotterCommand : plotter.getPlot()){
        if(plotterCommand instanceof ColorPlotterCommand){
          ColorPlotterCommand colorPlotterCommand = (ColorPlotterCommand) plotterCommand;          
          putIntoWater(writer);
          putIntoPaint(writer, colorPlotterCommand);          
        }
        else if(plotterCommand instanceof PathPlotterCommand){
          PathPlotterCommand pathPlotterCommand = (PathPlotterCommand) plotterCommand;
          drawLine(writer, pathPlotterCommand);
        }
      }
    }
    catch(IOException e){
      throw new RuntimeException(e);
    }
    finally{
      if(writer != null) {
        try{
          writer.close();
        }
        catch(IOException e){}
      }
    }
  }
  void putIntoWater(BufferedWriter writer) throws IOException {
    goDownShakeUp(writer, plotter.water);
  }
  
  void putIntoPaint(BufferedWriter writer, ColorPlotterCommand colorPlotterCommand) throws IOException {
    Rect colorBucket = plotter.palette.getIndexedColorRect(colorPlotterCommand.indexedColor);
    goDownShakeUp(writer, colorBucket);
  }
  void drawLine(BufferedWriter writer, PathPlotterCommand pathPlotterCommand) throws IOException {
    go(writer, pathPlotterCommand.x1, pathPlotterCommand.y1, UP);
    go(writer, pathPlotterCommand.x1, pathPlotterCommand.y1, CANVAS_DOWN);
    go(writer, pathPlotterCommand.x2, pathPlotterCommand.y2, CANVAS_DOWN);
    go(writer, pathPlotterCommand.x2, pathPlotterCommand.y2, UP);
  }
  void goDownShakeUp(BufferedWriter writer, Rect rect) throws IOException {
    if(rect.width >= rect.height){
      goFast(writer, rect.x + rect.width * MARGIN, rect.y + rect.height / 2, UP);
      goFast(writer, rect.x + rect.width * MARGIN, rect.y + rect.height / 2, BUCKET_DOWN);
      goFast(writer, rect.x + rect.width * ANTI_MARGIN, rect.y + rect.height / 2, BUCKET_DOWN);
      goFast(writer, rect.x + rect.width * ANTI_MARGIN, rect.y + rect.height / 2, UP);
    }
    else{
      goFast(writer, rect.x + rect.width / 2, rect.y + rect.height * MARGIN, UP);
      goFast(writer, rect.x + rect.width / 2, rect.y + rect.height * MARGIN, BUCKET_DOWN);
      goFast(writer, rect.x + rect.width / 2, rect.y + rect.height * ANTI_MARGIN, BUCKET_DOWN);
      goFast(writer, rect.x + rect.width / 2, rect.y + rect.height * ANTI_MARGIN, UP);
    }
  }

  void goFast(BufferedWriter writer, double x, double y, double z) throws IOException {
    writer.write("G00 X");
    finishGoLine(writer, x, y, z);
  }

  void go(BufferedWriter writer, double x, double y, double z) throws IOException {
    writer.write("G01 X");
    finishGoLine(writer, x, y, z);
  }
  
  void finishGoLine(BufferedWriter writer, double x, double y, double z) throws IOException {
    writer.write(String.valueOf((int)x));
    writer.write(" Y");
    writer.write(String.valueOf(plotterAreaHeight - (int)y));
    writer.write(" Z");
    writer.write(String.valueOf((int)z));
    writer.newLine();
  }
}