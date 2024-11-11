import React, { useEffect, useState } from "react";
import { Button, Table, Spin, Modal, message } from "antd";
import { useGetUserAll } from "../../../hooks/admin/UseGetUserAll";
import { EditOutlined } from "@ant-design/icons";
import { useDeleteUser } from "../../../hooks/admin/UseDeleteUser";
import { useGetAllUserByPage } from "../../../hooks/admin/UseGetAllUserByPage";
import { LOCAL_STORAGE_LOGIN_KEY } from "../../../constant/localStorage";
import { PATH } from "../../../constant";
import { Input, Space, Tag } from "antd";
import { filter } from "@chakra-ui/react";
import { ExclamationCircleOutlined } from '@ant-design/icons';

const { confirm } = Modal;

const { Search } = Input;

const ManageUser = () => {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [totalElements, setTotalElements] = useState(0);
  const {
    data: lstUser,
    refetch,
    isFetching,
  } = useGetAllUserByPage(currentPage - 1, pageSize);

  const mutate = useDeleteUser();
  const [filteredName, setFilteredName] = useState([]);
  const [loadingId, setLoadingId] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [selectAll, setSelectAll] = useState(false);
  const [isDeletingSelected, setIsDeletingSelected] = useState(false);

  useEffect(() => {
    refetch();
  }, []);


  useEffect(() => {
    if (lstUser) {
      setTotalElements(lstUser?.data?.totalElements);
      setCurrentPage(lstUser?.data?.number + 1);
    }
  }, [lstUser]);


  // const filteredUsers = lstUser?.filter((user) =>
  //   user.roles.some((role) => role.name === "ROLE_MEMBER")
  // );

  const onKeyUp = (e) => {
    const input = e?.target.value.toLowerCase();
    const filtered = lstUser?.data?.content?.filter((user) =>
      user.username.toLowerCase().includes(input)
    );
    setFilteredName(filtered || []);
    setCurrentPage(1); // Reset to first page when searching
  };

  const columns = [
    {
      title: <div style={{ textAlign: 'center' }}>User Name</div>,
      dataIndex: "username",
      width: "15%",
    },
    {
      title: <div style={{ textAlign: 'center' }}>Address</div>,
      dataIndex: "address",
      width: "20%",
      render: (address) => (
        <div style={{ textAlign: 'center' }}>
          {address ? (
            <Tag color="blue">{address}</Tag>
          ) : (
            <Tag color="red">Not Set</Tag>
          )}
        </div>
      ),
    },
    {
      title: <div style={{ textAlign: 'center' }}>Email</div>,
      dataIndex: "email",
      width: "20%",
    },
    {
      title: <div style={{ textAlign: 'center' }}>ROLE</div>,
      width: "70px",
      dataIndex: "id",
      showSorterTooltip: {
        target: "full-header",
      },
      render: (_, record) => {
        const hasRoleAdmin = record?.roles.some(
          (role) => role.name === "ROLE_ADMIN"
        );
        const hasRoleMember = record?.roles.some(
          (role) => role.name === "ROLE_MEMBER"
        );
        
        return (
          <div style={{ textAlign: 'center' }}>
            <Tag
              color={hasRoleAdmin ? "red" : hasRoleMember ? "green" : "blue"}
              style={{ width: '100px', textAlign: 'center' }}
            >
              {hasRoleAdmin ? "ADMIN" : hasRoleMember ? "MEMBER" : "CONTRIBUTOR"}
            </Tag>
          </div>
        );
      },
    },
    {
      title: <div style={{ textAlign: 'center' }}>Package</div>,
      dataIndex: "auserPackage",
      width: "20%",
      render: (auserPackage) => {
        if (!auserPackage?.name) {
          return <div style={{ textAlign: 'center' }}>No Package</div>;
        }

        let bgColor = '';
        switch (auserPackage.name.toLowerCase()) {
          case 'advanced':
            bgColor = '#FAF3E1';
            break;
          case 'professional':
            bgColor = '#F5E7C6';
            break;
          case 'vip':
            bgColor = '#FF6D1F';
            break;
          case 'svip':
            bgColor = '#222222';
            break;
          default:
            bgColor = 'transparent';
        }

        return (
          <div style={{ textAlign: 'center' }}>
            <div
              style={{
                border: '1px solid #d9d9d9',
                padding: '4px 8px',
                borderRadius: '4px',
                backgroundColor: bgColor,
                color: auserPackage.name.toLowerCase() === 'svip' ? 'white' : 'inherit',
                display: 'inline-block',
                width: '110px',
                textAlign: 'center',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                fontWeight: 'bold',
              }}
            >
              {auserPackage.name}
            </div>
          </div>
        );
      },
    },
    {
      title: <div style={{ textAlign: 'center' }}>Action</div>,
      dataIndex: "",
      key: "x",
      render: (_, user) => {
        return (
          <div style={{ textAlign: 'center' }}>
            <EditOutlined
                className="mr-[15px]"
                style={{ color: "blue" }}
              />
          </div>
        );
      },
      width: "10%",
    },
  ];

  const data = filteredName.length > 0 ? filteredName : lstUser?.data?.content;

  const onChange = (pagination, filters, sorter, extra) => {
    // console.log("params", pagination, filters, sorter, extra);
  };

  // if (isFetching) {
  //   return (
  //     <div className="flex justify-center top-0 bottom-0 left-0 right-0 items-center h-full">
  //       <Spin tip="Loading" size="large" />
  //     </div>
  //   );
  // }

  const rowSelection = {
    selectedRowKeys,
    onChange: (selectedRowKeys) => {
      setSelectedRowKeys(selectedRowKeys);
    },
    getCheckboxProps: (record) => ({
      disabled: record.roles.some((role) => role.name === "ROLE_ADMIN"),
    }),
  };

  // const handleSelectAll = () => {
  //   setSelectAll(true);
  //   setSelectedRowKeys(lstUser?.data?.content.map(user => user.id) || []);
  // };

  // const handleCancelSelection = () => {
  //   setSelectAll(false);
  //   setSelectedRowKeys([]);
  // };

  // const handleDeleteSelected = () => {
  //   confirm({
  //     title: 'Delete Selected Users',
  //     icon: <ExclamationCircleOutlined />,
  //     content: `Are you sure you want to delete ${selectedRowKeys.length} selected user(s)?`,
  //     okText: 'Yes',
  //     okType: 'danger',
  //     cancelText: 'No',
  //     onOk() {
  //       deleteSelectedUsers();
  //     },
  //   });
  // };

  const deleteSelectedUsers = async () => {
    setIsDeletingSelected(true);
    try {
      for (const id of selectedRowKeys) {
        await mutate.mutateAsync(id);
      }
      message.success("Selected users deleted successfully!");
      setSelectedRowKeys([]);
      refetch();
    } catch (error) {
      message.error(`Error deleting users: ${error.message}`);
    } finally {
      setIsDeletingSelected(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">

      <Search
        style={{ marginBottom: "20px" }}
        placeholder="Search by username"
        allowClear
        size="large"
        onKeyUp={onKeyUp}
      />

      <Table
        columns={columns.map(column => {
          if (column.title === "Action") {
            return {
              ...column,
              render: (_, user) => (
                <div key={user.id}>
                   <EditOutlined
                      className="mr-[15px]"
                      style={{ color: "blue" }}
                    />
                </div>
              ),
            };
          }
          return column;
        })}
        dataSource={data}
        rowKey="id"
        onChange={onChange}
        loading={isFetching}
        pagination={{
          current: currentPage,
          pageSize: pageSize,
          total: totalElements,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) =>
            `${range[0]}-${range[1]} of ${total} items`,
          onChange: (page, pageSize) => {
            setCurrentPage(page);
            setPageSize(pageSize);
          },
        }}
        className="shadow-lg rounded-lg overflow-hidden"
        bordered={false}
      />
    </div>
  );
};

export default ManageUser;
