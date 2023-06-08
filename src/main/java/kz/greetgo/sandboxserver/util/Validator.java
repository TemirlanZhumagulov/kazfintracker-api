package kz.greetgo.sandboxserver.util;

import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;

public class Validator {

  public static void validateA(TestModelAToUpsert testModel, boolean isCreate) {
    if (testModel == null) {
      throw new IllegalArgumentException("testModel cannot be null");
    }

    if (!isCreate && StrUtils.isNullOrBlank(testModel.id)) {
      throw new IllegalArgumentException("id cannot be null");
    }

    if (StrUtils.isNullOrBlank(testModel.strField)) {
      throw new IllegalArgumentException("strField cannot be null");
    }

    if (testModel.boolField == null) {
      throw new IllegalArgumentException("boolField cannot be null");
    }

    if (testModel.intField == null) {
      throw new IllegalArgumentException("intField cannot be null");
    }

    // business logic
    if (testModel.intField < 0 || testModel.intField > 1_000_000) {
      throw new IllegalArgumentException("intField is expected to be in range from 0 to 1 000 000");
    }

  }

}
