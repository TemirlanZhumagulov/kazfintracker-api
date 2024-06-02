package kz.kazfintracker.sandboxserver.mappers;

import kz.kazfintracker.sandboxserver.model.mongo.*;
import kz.kazfintracker.sandboxserver.model.web.*;
import kz.kazfintracker.sandboxserver.util.Ids;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = Ids.class)
public interface FinMapper {

  FinMapper INSTANCE = Mappers.getMapper(FinMapper.class);

  @Mapping(target = "id", source = "id", qualifiedByName = "intToObjectId")
  TransactionDto toDto(Transaction transaction);

  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToInt")
  Transaction fromDto(TransactionDto transactionDto);

  @Mapping(target = "id", source = "id", qualifiedByName = "intToObjectId")
  BankAccountDto toDto(BankAccount bankAccount);

  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToInt")
  BankAccount fromDto(BankAccountDto bankAccountDto);

  @Mapping(target = "id", source = "id", qualifiedByName = "intToObjectId")
  RecurringTransactionAmountDto toDto(RecurringTransactionAmount rta);

  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToInt")
  RecurringTransactionAmount fromDto(RecurringTransactionAmountDto dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "intToObjectId")
  CurrencyDto toDto(Currency currency);

  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToInt")
  Currency fromDto(CurrencyDto dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "intToObjectId")
  CategoryTransactionDto toDto(CategoryTransaction categoryTransaction);

  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToInt")
  CategoryTransaction fromDto(CategoryTransactionDto dto);

  @Mapping(target = "id", source = "id", qualifiedByName = "intToObjectId")
  BudgetDto toDto(Budget budget);

  @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToInt")
  Budget fromDto(BudgetDto budgetDto);

}
