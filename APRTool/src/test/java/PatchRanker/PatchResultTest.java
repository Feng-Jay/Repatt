package PatchRanker;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class PatchResultTest {
  public static void main(String[] args) throws IOException {
    PatchResult RepattResult = new PatchResult("/Users/higgs/Desktop/SBFLPatches/Repatt","Repatt");
    PatchResult TBarResult = new PatchResult("/Users/higgs/Desktop/SBFLPatches/TBar","TBar");
    PatchResult SimFixResult = new PatchResult("/Users/higgs/Desktop/SBFLPatches/SimFix","SimFix");
    PatchResult TransplantFixResult = new PatchResult("/Users/higgs/Desktop/SBFLPatches/TransplantFix","TransplantFix");
    PatchRanker patchRanker = new PatchRanker();
    patchRanker.addPatchResult(RepattResult);
    patchRanker.addPatchResult(SimFixResult);
    patchRanker.addPatchResult(TBarResult);
    patchRanker.addPatchResult(TransplantFixResult);
    String d4jPath = "/Users/higgs/PycharmProjects/gumtree/d4j/%s_%s";
    patchRanker.anchorPatch(d4jPath);
    patchRanker.rank(new GumtreeGrader());
    patchRanker.saveResultTo("/Users/higgs/Desktop/SBFLRankResultNew.txt");
  }

}